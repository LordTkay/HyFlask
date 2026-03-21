package de.lordtkay.hyflask.effect.ui

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.validation.Validators
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue
import com.hypixel.hytale.server.core.ui.PatchStyle
import com.hypixel.hytale.server.core.ui.Value
import com.hypixel.hytale.server.core.ui.builder.EventData
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.MessageUtil
import de.lordtkay.hyflask.effect.asset.FlaskEffect
import de.lordtkay.hyflask.effect.asset.FlaskEffectGroup
import de.lordtkay.hyflask.effect.component.FlaskEffectComponent
import de.lordtkay.hyflask.effect.ui.FlaskEffectSelectionPage.EventType.*
import de.lordtkay.hyflask.effect.ui.event.ActivateEffectUiCommand
import de.lordtkay.hyflask.effect.ui.event.DeactivateEffectUiCommand
import de.lordtkay.hyflask.effect.ui.event.DecreaseLevelUiCommand
import de.lordtkay.hyflask.effect.ui.event.IncreaseLevelUiCommand
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStat
import de.lordtkay.hyflask.utility.ui.command.UiCommandManager
import java.util.*

// TODO: Implement the search functionality
// TODO: Undo/Redo on Apply should also work

class FlaskEffectSelectionPage(
    playerRef: PlayerRef,
    val flaskEffectComponent: FlaskEffectComponent,
    entityStatMap: EntityStatMap
) : InteractiveCustomUIPage<FlaskEffectSelectionPage.FlaskEffectSelectionEventData>(
    playerRef,
    CustomPageLifetime.CanDismiss,
    FlaskEffectSelectionEventData.CODEC
) {

    private val capacityStat: EntityStatValue
    private val initiator = UiCommandManager()
    private val activeGroups: MutableMap<String, EffectGroup> = TreeMap()
    private val learnedGroups: MutableMap<String, EffectGroup> = TreeMap()

    init {
        val capacityIndex = HyFlaskEntityStat.CAPACITY.getIndex()
        val capacityStat = entityStatMap.get(capacityIndex)
        if (capacityStat == null) {
            logger.atWarning()
                .log("Could not find entity stat ${HyFlaskEntityStat.CAPACITY.id} with Index $capacityIndex")
        }
        this.capacityStat = capacityStat!!
    }

    override fun build(
        ref: Ref<EntityStore?>,
        commandBuilder: UICommandBuilder,
        eventBuilder: UIEventBuilder,
        store: Store<EntityStore?>
    ) {
        commandBuilder.append("Pages/FlaskEffectSelectionPage.ui")
        commandBuilder.clear("#LearnedEffects #Content")
        commandBuilder.clear("#ActiveEffects #EffectList")

        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#UndoButton",
            EventData.of(
                "EventType", UNDO.name
            )
        )

        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#RedoButton",
            EventData.of(
                "EventType", REDO.name
            )
        )

        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#ApplyButton",
            EventData.of(
                "EventType", APPLY.name
            )
        )

        val groups = groupEffects()

        groups.values.forEach { group ->
            val activeEffect = group.activeEffect
            if (activeEffect != null) {
                activeGroups[group.name] = group
            } else {
                learnedGroups[group.name] = group
            }
        }

        activeGroups.values.forEachIndexed { index, group ->
            commandBuilder.append("#ActiveEffects #EffectList", "Pages/FlaskEffectActiveItem.ui")
            applyActiveEffectElement(commandBuilder, eventBuilder, group, index)
        }

        learnedGroups.values.forEachIndexed { index, group ->
            commandBuilder.append("#LearnedEffects #Content", "Pages/FlaskEffectLearnedItem.ui")
            applyLearnedEffectElement(commandBuilder, eventBuilder, group, index)
        }

        applyCost(commandBuilder)
    }

    private fun groupEffects(): Map<String, EffectGroup> {
        val groups = mutableMapOf<String, EffectGroup>()

        flaskEffectComponent.learnedEffects.forEach { effectId ->
            val asset = FlaskEffect.assetMap.getAsset(effectId) ?: return@forEach

            val effectGroup = asset.groupDetails
            val currentLevel = asset.groupDetails?.level ?: 1

            // TODO: Change the costs to the asset one
            // TODO: Logic could be improved

            var isActiveEffect = flaskEffectComponent.isActive(asset.id)
            var groupId: String
            var entry: EffectGroup

            if (effectGroup == null) {
                groupId = asset.id
                entry = EffectGroup(asset.displayName, asset.icon, asset.cost, asset.cost)
            } else {
                val group = FlaskEffectGroup.assetMap.getAsset(effectGroup.id) ?: return@forEach
                groupId = group.id

                if (groupId !in groups) {
                    entry = EffectGroup(group.displayName, group.icon, asset.cost, asset.cost)
                } else {
                    entry = groups[groupId]!!

                    if (asset.cost < entry.minCost) {
                        entry.minCost = asset.cost
                    } else if (asset.cost > entry.maxCost) {
                        entry.maxCost = asset.cost
                    }
                }

                val activeLevel = entry.activeEffect?.groupDetails?.level ?: 1
                if (activeLevel > currentLevel) {
                    isActiveEffect = false
                }
            }

            entry.learnedEffects[currentLevel] = asset

            if (isActiveEffect) {
                entry.activeEffect = asset
            }

            groups[groupId] = entry
        }
        return groups.toMap()
    }

    private fun applyCost(commandBuilder: UICommandBuilder, applyButtonDisabled: Boolean = true) {
        val totalCost = activeGroups.values.sumOf { it.activeEffect?.cost ?: 0 }
        val capacity = capacityStat.max
        val percentage = if (capacity > 0) {
            totalCost / capacity
        } else {
            0f
        }

        commandBuilder.set("#CapacityCurrent.Text", totalCost.toString())
        commandBuilder.set("#CapacityErrorCurrent.Text", totalCost.toString())
        commandBuilder.set("#CapacityMax.Text", capacity.toInt().toString())

        commandBuilder.set("#CapacityProgressBar.Value", percentage)
        commandBuilder.set("#CapacityProgressBarEffect.Value", percentage)
        commandBuilder.set("#ApplyButton.Disabled", applyButtonDisabled || totalCost > capacity)

        commandBuilder.set("#CapacityProgress.Visible", totalCost <= capacity)
        commandBuilder.set("#CapacityCurrent.Visible", totalCost <= capacity)
        commandBuilder.set("#CapacityErrorProgress.Visible", totalCost > capacity)
        commandBuilder.set("#CapacityErrorCurrent.Visible", totalCost > capacity)
    }

    override fun handleDataEvent(
        ref: Ref<EntityStore?>,
        store: Store<EntityStore?>,
        data: FlaskEffectSelectionEventData
    ) {
        super.handleDataEvent(ref, store, data)
        val commandBuilder = UICommandBuilder()
        val eventBuilder = UIEventBuilder()

        val command = when (data.eventType) {
            INCREASE_LEVEL -> activeGroups[data.groupName]?.let {
                IncreaseLevelUiCommand(activeGroups, it)
            }

            DECREASE_LEVEL -> activeGroups[data.groupName]?.let {
                DecreaseLevelUiCommand(activeGroups, it)
            }

            ACTIVATE_EFFECT -> learnedGroups[data.groupName]?.let {
                ActivateEffectUiCommand(activeGroups, learnedGroups, it)
            }

            DEACTIVATE_EFFECT -> activeGroups[data.groupName]?.let {
                DeactivateEffectUiCommand(activeGroups, learnedGroups, it)
            }

            UNDO -> {
                initiator.undo(commandBuilder, eventBuilder)
                null
            }

            REDO -> {
                initiator.redo(commandBuilder, eventBuilder)
                null
            }

            APPLY -> {
                flaskEffectComponent.deactivateAllEffect(ref, store)

                activeGroups.values.forEach { group ->
                    flaskEffectComponent.activateEffect(group.activeEffect!!.id, ref, store)
                }

                null
            }
        }

        if (command != null) {
            initiator.execute(commandBuilder, eventBuilder, command)
        }

        commandBuilder.set("#RedoButton.Disabled", !initiator.hasRedoHistory)
        commandBuilder.set("#UndoButton.Disabled", !initiator.hasHistory)
        applyCost(commandBuilder, !initiator.hasHistory || data.eventType == APPLY)

        sendUpdate(commandBuilder, eventBuilder, false)
    }

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()

        fun getActiveEffectSelector(index: Int): String = "#ActiveEffects #EffectList[$index]"
        fun getLearnedEffectSelector(index: Int): String = "#LearnedEffects #Content[$index]"

        fun applyActiveEffectElement(
            commandBuilder: UICommandBuilder,
            eventBuilder: UIEventBuilder,
            group: EffectGroup,
            index: Int
        ) {
            val activeEffect = group.activeEffect ?: return
            val currentLevel = activeEffect.groupDetails?.level ?: 1
            val selector = getActiveEffectSelector(index)

            val levelMessage = Message.translation("server.hyflask.ui.effectSelection.level.title")
            val level = MessageUtil.toAnsiString(levelMessage).toString()

            commandBuilder.set("$selector #ItemLabel.Text", activeEffect.displayName)
            commandBuilder.set("$selector #ItemText.TooltipText", activeEffect.displayName)
            commandBuilder.set("$selector #ItemDescription.Text", activeEffect.description ?: "")

            commandBuilder.set("$selector #ItemCost.Text", activeEffect.cost.toString())
            commandBuilder.set("$selector #ItemLevel.Text", "$level $currentLevel")

            commandBuilder.set("$selector #IncreaseLevelButton.Visible", group.learnedEffects.size > 1)
            commandBuilder.set("$selector #DecreaseLevelButton.Visible", group.learnedEffects.size > 1)
            commandBuilder.set("$selector #IncreaseLevelButton.Disabled", currentLevel >= group.learnedEffects.size)
            commandBuilder.set("$selector #DecreaseLevelButton.Disabled", currentLevel <= 1)

            val quality = ItemQuality.getAssetMap().getAsset(activeEffect.qualityIndex)
            if (quality != null) {
                val colorHex = String.format(
                    "#%02X%02X%02X",
                    quality.textColor.red,
                    quality.textColor.green,
                    quality.textColor.blue
                )
                commandBuilder.set("$selector #ItemLabel.Style.TextColor", colorHex)
                commandBuilder.setObject(
                    "$selector #Item.Background", PatchStyle(
                        Value.of("Borders/Border${quality.id}.png"),
                        Value.of(20)
                    )
                )
            }

            val icon = activeEffect.icon
            if (icon != null) {
                commandBuilder.set("$selector #ItemIcon.AssetPath", icon)
            }

            eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "$selector #IncreaseLevelButton",
                EventData.of(
                    "EventType", INCREASE_LEVEL.name
                ).append(
                    "GroupName", group.name
                )
            )

            eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "$selector #DecreaseLevelButton",
                EventData.of(
                    "EventType", DECREASE_LEVEL.name
                ).append(
                    "GroupName", group.name
                )
            )

            eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "$selector #RemoveButton",
                EventData.of(
                    "EventType", DEACTIVATE_EFFECT.name
                ).append(
                    "GroupName", group.name
                )
            )
        }


        fun applyLearnedEffectElement(
            commandBuilder: UICommandBuilder,
            eventBuilder: UIEventBuilder,
            group: EffectGroup,
            index: Int
        ) {
            val selector = getLearnedEffectSelector(index)

            commandBuilder.set("$selector #ItemLabel.Text", group.name)
            commandBuilder.set("$selector #ItemLabel.TooltipText", group.name)


            var costRange = group.minCost.toString()
            if (group.minCost != group.maxCost) {
                costRange = "${group.minCost} - ${group.maxCost}"
            }
            commandBuilder.set("$selector #ItemCostRange.Text", costRange)

            val icon = group.icon
            if (icon != null) {
                commandBuilder.set("$selector #ItemIcon.AssetPath", icon)
            }

            eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "$selector #AddButton",
                EventData.of(
                    "EventType", ACTIVATE_EFFECT.name
                ).append(
                    "GroupName", group.name
                )
            )
        }
    }

    class EffectGroup(
        val name: String,
        val icon: String?,
        var minCost: Int,
        var maxCost: Int,
        var learnedEffects: TreeMap<Int, FlaskEffect> = TreeMap(),
        var activeEffect: FlaskEffect? = null
    )

    enum class EventType {
        INCREASE_LEVEL,
        DECREASE_LEVEL,
        ACTIVATE_EFFECT,
        DEACTIVATE_EFFECT,
        UNDO,
        REDO,
        APPLY
    }

    class FlaskEffectSelectionEventData {
        var groupName: String? = null
        lateinit var eventType: EventType

        companion object {
            val CODEC: BuilderCodec<FlaskEffectSelectionEventData?>

            init {
                val builder = BuilderCodec.builder(
                    FlaskEffectSelectionEventData::class.java,
                    ::FlaskEffectSelectionEventData
                )

                builder
                    .append(
                        KeyedCodec("GroupName", Codec.STRING),
                        { data: FlaskEffectSelectionEventData, value: String? ->
                            data.groupName = value
                        },
                        { data: FlaskEffectSelectionEventData ->
                            data.groupName
                        }
                    )
                    .add()

                builder
                    .append(
                        KeyedCodec("EventType", Codec.STRING),
                        { data: FlaskEffectSelectionEventData, value: String ->
                            data.eventType = EventType.valueOf(value)
                        },
                        { data: FlaskEffectSelectionEventData ->
                            data.eventType.name
                        }
                    )
                    .addValidator(Validators.nonNull())
                    .add()


                CODEC = builder.build()
            }
        }
    }
}