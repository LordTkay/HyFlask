package de.lordtkay.hyflask.effect.ui

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.validation.Validators
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage
import com.hypixel.hytale.server.core.ui.PatchStyle
import com.hypixel.hytale.server.core.ui.Value
import com.hypixel.hytale.server.core.ui.builder.EventData
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.effect.asset.FlaskEffect
import de.lordtkay.hyflask.effect.asset.FlaskEffectGroup
import de.lordtkay.hyflask.effect.component.FlaskEffectComponent
import java.util.*

// TODO: Texts need to be in the server.lang
// TODO: Effects of the same family/group need to be put together and their family/group content should be displayed

class FlaskEffectSelectionPage(
    playerRef: PlayerRef,
    val flaskEffectComponent: FlaskEffectComponent
) : InteractiveCustomUIPage<FlaskEffectSelectionPage.FlaskEffectSelectionEventData>(
    playerRef,
    CustomPageLifetime.CanDismiss,
    FlaskEffectSelectionEventData.CODEC
) {

    private var activeGroups: MutableList<EffectGroup> = mutableListOf()
    private var learnedGroups: MutableList<EffectGroup> = mutableListOf()

    override fun build(
        ref: Ref<EntityStore?>,
        commandBuilder: UICommandBuilder,
        eventBuilder: UIEventBuilder,
        store: Store<EntityStore?>
    ) {
        commandBuilder.append("Pages/FlaskEffectSelectionPage.ui")
        commandBuilder.clear("#LearnedEffects #Content")
        commandBuilder.clear("#ActiveEffects #EffectList")

        val groups = groupEffects()

        groups.values.forEach { group ->
            val activeEffect = group.activeEffect
            if (activeEffect != null) {
                activeGroups.add(group)
                commandBuilder.append("#ActiveEffects #EffectList", "Pages/FlaskEffectActiveItem.ui")
                applyActiveEffectElement(commandBuilder, eventBuilder, activeGroups.size - 1, group)
            } else {
                learnedGroups.add(group)
                commandBuilder.append("#LearnedEffects #Content", "Pages/FlaskEffectLearnedItem.ui")
                applyLearnedEffectElement(commandBuilder, eventBuilder, learnedGroups.size - 1, group)
            }
        }
    }

    private fun applyActiveEffectElement(
        commandBuilder: UICommandBuilder,
        eventBuilder: UIEventBuilder,
        index: Int,
        group: EffectGroup
    ) {
        val activeEffect = group.activeEffect ?: return
        val currentLevel = activeEffect.groupDetails?.level ?: 1
        val selector = "#ActiveEffects #EffectList[$index]"

        commandBuilder.set("$selector #ItemLabel.Text", activeEffect.displayName)
        commandBuilder.set("$selector #ItemText.TooltipText", activeEffect.displayName)
        commandBuilder.set("$selector #ItemDescription.Text", activeEffect.description ?: "")

        commandBuilder.set("$selector #ItemCost.Text", "5")
        commandBuilder.set("$selector #ItemLevel.Text", "LV $currentLevel")

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

        val currentIndex = activeGroups.indexOf(group)
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "$selector #IncreaseLevelButton",
            EventData.of(
                "EventType", EventType.INCREASE_LEVEL.name
            ).append(
                "TargetIndex", currentIndex.toString()
            )
        )

        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "$selector #DecreaseLevelButton",
            EventData.of(
                "EventType", EventType.DECREASE_LEVEL.name
            ).append(
                "TargetIndex", currentIndex.toString()
            )
        )
    }

    private fun applyLearnedEffectElement(
        commandBuilder: UICommandBuilder,
        eventBuilder: UIEventBuilder,
        index: Int,
        group: EffectGroup
    ) {
        val selector = "#LearnedEffects #Content[$index]"

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
                entry = EffectGroup(asset.displayName, asset.icon, 5, 5)
            } else {
                val group = FlaskEffectGroup.assetMap.getAsset(effectGroup.id) ?: return@forEach
                groupId = group.id

                if (groupId !in groups) {
                    entry = EffectGroup(group.displayName, group.icon, 5, 5)
                } else {
                    entry = groups[groupId]!!
                    entry.maxCost = 10
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

    override fun handleDataEvent(
        ref: Ref<EntityStore?>,
        store: Store<EntityStore?>,
        data: FlaskEffectSelectionEventData
    ) {
        super.handleDataEvent(ref, store, data)
        val commandBuilder = UICommandBuilder()

        if (data.eventType == EventType.INCREASE_LEVEL || data.eventType == EventType.DECREASE_LEVEL) {
            val targetGroup = activeGroups[data.targetIndex]
            val activeEffect = targetGroup.activeEffect

            if (activeEffect != null) {
                val activeIndex = activeEffect.groupDetails?.level ?: 1

                val nextLevel = if (data.eventType === EventType.INCREASE_LEVEL) {
                    targetGroup.learnedEffects.higherEntry(activeIndex)
                } else {
                    targetGroup.learnedEffects.lowerEntry(activeIndex)
                }

                if (nextLevel != null) {
                    targetGroup.activeEffect = nextLevel.value
                }
            } else {
                targetGroup.activeEffect = targetGroup.learnedEffects.firstEntry().value
            }

            applyActiveEffectElement(commandBuilder, UIEventBuilder(), data.targetIndex, targetGroup)
        }

        sendUpdate(commandBuilder)
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
        DECREASE_LEVEL
    }

    class FlaskEffectSelectionEventData {
        var targetIndex: Int = 0
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
                        KeyedCodec("TargetIndex", Codec.STRING),
                        { data: FlaskEffectSelectionEventData, value: String ->
                            data.targetIndex = value.toInt()
                        },
                        { data: FlaskEffectSelectionEventData ->
                            data.targetIndex.toString()
                        }
                    )
                    .addValidator(Validators.nonNull())
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