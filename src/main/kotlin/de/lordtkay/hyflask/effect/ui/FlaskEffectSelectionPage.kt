package de.lordtkay.hyflask.effect.ui

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage
import com.hypixel.hytale.server.core.ui.PatchStyle
import com.hypixel.hytale.server.core.ui.Value
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.effect.asset.FlaskEffect
import de.lordtkay.hyflask.effect.asset.FlaskEffectGroup
import de.lordtkay.hyflask.effect.component.FlaskEffectComponent

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

        var learnedCount = 0
        var activeCount = 0

        groups.values.forEach { group ->
            val activeEffect = group.activeEffect
            if (activeEffect != null) {
                if (activeCount > 0) {
                    commandBuilder.append("#ActiveEffects #EffectList", "Pages/FlaskEffectSpacerItem.ui")
                    activeCount++
                }
                applyActiveEffectElement(activeCount, commandBuilder, activeEffect, group)
                activeCount++
            } else {
                if (learnedCount > 0) {
                    commandBuilder.append("#LearnedEffects #Content", "Pages/FlaskEffectSpacerItem.ui")
                    learnedCount++
                }
                applyLearnedEffectElement(learnedCount, commandBuilder, group)
                learnedCount++
            }
        }

    }

    private fun applyActiveEffectElement(
        index: Int,
        commandBuilder: UICommandBuilder,
        activeEffect: FlaskEffect,
        group: EffectGroup
    ) {
        val currentLevel = activeEffect.groupDetails?.level ?: 1

        commandBuilder.append("#ActiveEffects #EffectList", "Pages/FlaskEffectActiveItem.ui")
        val selector = "#ActiveEffects #EffectList[$index]"

        commandBuilder.set("$selector #ItemLabel.Text", activeEffect.displayName)
        commandBuilder.set("$selector #ItemText.TooltipText", activeEffect.displayName)
        commandBuilder.set("$selector #ItemDescription.Text", activeEffect.description ?: "")

        commandBuilder.set("$selector #ItemCost.Text", "5")
        commandBuilder.set("$selector #ItemLevel.Text", "LV $currentLevel")

        if (group.learnedEffects.size > 1) {
            commandBuilder.set("$selector #IncreaseLevelButton.Visible", true)
            commandBuilder.set("$selector #DecreaseLevelButton.Visible", true)
        }

        if (group.learnedEffects.size > currentLevel) {
            commandBuilder.set("$selector #IncreaseLevelButton.Disabled", false)
        }
        if (currentLevel > 1) {
            commandBuilder.set("$selector #DecreaseLevelButton.Disabled", false)
        }

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
                "$selector.Background", PatchStyle(
                    Value.of("Borders/Border${quality.id}.png"),
                    Value.of(20)
                )
            )
        }


        val icon = activeEffect.icon
        if (icon != null) {
            commandBuilder.set("$selector #ItemIcon.AssetPath", icon)
        }
    }

    private fun applyLearnedEffectElement(
        index: Int,
        commandBuilder: UICommandBuilder,
        group: EffectGroup
    ) {
        commandBuilder.append("#LearnedEffects #Content", "Pages/FlaskEffectLearnedItem.ui")
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

                val activeLevel = entry.activeEffect?.groupDetails?.level ?: 0
                val currentLevel = asset.groupDetails?.level ?: 0
                if (activeLevel > currentLevel) {
                    isActiveEffect = false
                }
            }

            entry.learnedEffects.add(asset)

            if (isActiveEffect) {
                entry.activeEffect = asset
            }

            groups[groupId] = entry
        }
        return groups.toMap()
    }

    class EffectGroup(
        val name: String,
        val icon: String?,
        var minCost: Int,
        var maxCost: Int,
        var learnedEffects: MutableList<FlaskEffect> = mutableListOf(),
        var activeEffect: FlaskEffect? = null
    )

    class FlaskEffectSelectionEventData {
        var selectedEffect: String? = null
            private set

        companion object {
            val CODEC: BuilderCodec<FlaskEffectSelectionEventData?>

            init {
                val builder = BuilderCodec.builder(
                    FlaskEffectSelectionEventData::class.java,
                    ::FlaskEffectSelectionEventData
                )

                builder.append(
                    KeyedCodec("SelectedEffect", Codec.STRING),
                    { data: FlaskEffectSelectionEventData, value: String? ->
                        if (value == null) return@append
                        data.selectedEffect = value
                    },
                    { data: FlaskEffectSelectionEventData ->
                        data.selectedEffect
                    }
                ).add()

                CODEC = builder.build()
            }
        }
    }
}