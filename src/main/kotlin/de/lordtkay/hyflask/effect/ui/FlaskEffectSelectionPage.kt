package de.lordtkay.hyflask.effect.ui

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.effect.asset.FlaskEffect
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

        var learnedCount = 0
        flaskEffectComponent.learnedEffects.forEach { effectId ->
            val asset = FlaskEffect.assetMap.getAsset(effectId) ?: return@forEach

            if (learnedCount > 0) {
                commandBuilder.append("#LearnedEffects #Content", "Pages/FlaskEffectSpacerItem.ui")
                learnedCount++
            }

            commandBuilder.append("#LearnedEffects #Content", "Pages/FlaskEffectLearnedItem.ui")
            val selector = "#LearnedEffects #Content[$learnedCount]"

            commandBuilder.set("$selector #ItemLabel.Text", asset.displayName)
            commandBuilder.set("$selector #ItemLabel.TooltipText", asset.displayName)

            commandBuilder.set("$selector #ItemCostRange.Text", "5 - 10")

            val icon = asset.icon
            if (icon != null) {
                commandBuilder.set("$selector #ItemIcon.AssetPath", icon)
            }

            learnedCount++
        }

        var activeCount = 0
        flaskEffectComponent.activeEffects.forEach { effectId ->
            val asset = FlaskEffect.assetMap.getAsset(effectId) ?: return@forEach

            if (activeCount > 0) {
                commandBuilder.append("#ActiveEffects #EffectList", "Pages/FlaskEffectSpacerItem.ui")
                activeCount++
            }

            commandBuilder.append("#ActiveEffects #EffectList", "Pages/FlaskEffectActiveItem.ui")
            val selector = "#ActiveEffects #EffectList[$activeCount]"

            commandBuilder.set("$selector #ItemLabel.Text", asset.displayName)
            commandBuilder.set("$selector #ItemText.TooltipText", asset.displayName)
            commandBuilder.set("$selector #ItemDescription.Text", asset.description ?: "")

            commandBuilder.set("$selector #ItemCost.Text", "5")
            commandBuilder.set("$selector #ItemLevel.Text", "LV 1")

            val quality = ItemQuality.getAssetMap().getAsset(asset.qualityIndex)
            if (quality != null) {
                val colorHex = String.format(
                    "#%02X%02X%02X",
                    quality.textColor.red,
                    quality.textColor.green,
                    quality.textColor.blue
                )
                commandBuilder.set("$selector #ItemLabel.Style.TextColor", colorHex)
//                commandBuilder.set("$selector.Background.TexturePath", "../Borders/Border${quality.id}.png")
            }


            val icon = asset.icon
            if (icon != null) {
                commandBuilder.set("$selector #ItemIcon.AssetPath", icon)
            }

            activeCount++
        }
    }

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