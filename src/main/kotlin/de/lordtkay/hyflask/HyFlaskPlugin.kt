package de.lordtkay.hyflask

import com.hypixel.hytale.assetstore.map.IndexedAssetMap
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.asset.HytaleAssetStore
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.Condition
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.util.Config
import de.lordtkay.hyflask.command.HyFlaskCommandCollection
import de.lordtkay.hyflask.config.FlaskConfig
import de.lordtkay.hyflask.effect.asset.FlaskEffect
import de.lordtkay.hyflask.effect.asset.FlaskEffectGroup
import de.lordtkay.hyflask.effect.component.FlaskEffectComponent
import de.lordtkay.hyflask.effect.content.jumpheight.JumpHeightComponent
import de.lordtkay.hyflask.effect.content.jumpheight.JumpHeightSystem
import de.lordtkay.hyflask.effect.content.jumpheight.ModifyJumpHeightInteraction
import de.lordtkay.hyflask.effect.content.recall.RecallComponent
import de.lordtkay.hyflask.effect.content.recall.RecallSystem
import de.lordtkay.hyflask.effect.content.waterbreathing.WaterBreathingCondition
import de.lordtkay.hyflask.effect.interaction.FlaskEffectApplyInteraction
import de.lordtkay.hyflask.effect.interaction.FlaskEffectForgetInteraction
import de.lordtkay.hyflask.effect.interaction.FlaskEffectLearnInteraction
import de.lordtkay.hyflask.effect.interaction.FlaskEffectRequireInteraction
import de.lordtkay.hyflask.effect.ui.FlaskEffectSelectionSupplier
import de.lordtkay.hyflask.enumeration.HyFlaskComponent
import de.lordtkay.hyflask.enumeration.HyFlaskCondition
import de.lordtkay.hyflask.enumeration.HyFlaskInteraction
import de.lordtkay.hyflask.enumeration.HyFlaskPage
import de.lordtkay.hyflask.uses.condition.SleptCondition
import de.lordtkay.hyflask.uses.interaction.HasUsesInteraction
import de.lordtkay.hyflask.uses.interaction.ModifyUsesInteraction

@Suppress("unused")
class HyFlaskPlugin(init: JavaPluginInit) : JavaPlugin(init) {

    companion object {
        private val logger = HytaleLogger.forEnclosingClass()
        var instance: HyFlaskPlugin? = null
            private set
    }

    val config: Config<FlaskConfig> = this.withConfig(FlaskConfig.CONFIG_NAME, FlaskConfig.CODEC)

    init {
        instance = this
    }

    override fun setup() {
        logger.atInfo().log("[$name] Setting up...")

        config.save()

        registerAssetStores()
        registerComponents()
        registerInteractions()
        registerSystems()
        registerPages()
        registerConditions()
        commandRegistry.registerCommand(HyFlaskCommandCollection())

        logger.atInfo().log("[$name] Setup complete!")
    }

    private fun registerConditions() {
        getCodecRegistry(Condition.CODEC).register(
            HyFlaskCondition.WATER_BREATHING.id,
            WaterBreathingCondition::class.java,
            WaterBreathingCondition.CODEC
        )

        getCodecRegistry(Condition.CODEC).register(
            HyFlaskCondition.SLEPT.id,
            SleptCondition::class.java,
            SleptCondition.CODEC
        )
    }

    private fun registerSystems() {
        entityStoreRegistry.registerSystem(RecallSystem())
        entityStoreRegistry.registerSystem(JumpHeightSystem())
    }

    private fun registerAssetStores() {

        val flaskEffectGroupAssetStore = HytaleAssetStore
            .builder(
                String::class.java,
                FlaskEffectGroup::class.java,
                IndexedAssetMap<String, FlaskEffectGroup>()
            )
            .setCodec(FlaskEffectGroup.CODEC)
            .setPath(FlaskEffectGroup.ASSET_PATH)
            .setKeyFunction { asset -> asset.id }
            .setReplaceOnRemove { id -> FlaskEffectGroup(id) }
            .build()

        assetRegistry.register(flaskEffectGroupAssetStore)


        val flaskEffectAssetStore = HytaleAssetStore
            .builder(
                String::class.java,
                FlaskEffect::class.java,
                IndexedAssetMap<String, FlaskEffect>()
            )
            .setCodec(FlaskEffect.CODEC)
            .setPath(FlaskEffect.ASSET_PATH)
            .setKeyFunction { asset -> asset.id }
            .setReplaceOnRemove { id -> FlaskEffect(id) }
            .build()

        assetRegistry.register(flaskEffectAssetStore)
    }


    private fun registerComponents() {
        val flaskEffectComponent = entityStoreRegistry.registerComponent(
            FlaskEffectComponent::class.java,
            HyFlaskComponent.FLASK_EFFECT.id,
            FlaskEffectComponent.CODEC
        )
        FlaskEffectComponent.componentType = flaskEffectComponent

        val recallComponent = entityStoreRegistry.registerComponent(
            RecallComponent::class.java,
            HyFlaskComponent.RECALL.id,
            RecallComponent.CODEC
        )
        RecallComponent.componentType = recallComponent

        val jumpHeightComponent = entityStoreRegistry.registerComponent(
            JumpHeightComponent::class.java,
            HyFlaskComponent.JUMP_HEIGHT.id,
            JumpHeightComponent.CODEC
        )
        JumpHeightComponent.componentType = jumpHeightComponent
    }

    private fun registerInteractions() {
        getCodecRegistry(Interaction.CODEC).register(
            HyFlaskInteraction.APPLY_EFFECT.id,
            FlaskEffectApplyInteraction::class.java,
            FlaskEffectApplyInteraction.CODEC
        )

        getCodecRegistry(Interaction.CODEC).register(
            HyFlaskInteraction.LEARN_EFFECT.id,
            FlaskEffectLearnInteraction::class.java,
            FlaskEffectLearnInteraction.CODEC
        )

        getCodecRegistry(Interaction.CODEC).register(
            HyFlaskInteraction.FORGET_EFFECT.id,
            FlaskEffectForgetInteraction::class.java,
            FlaskEffectForgetInteraction.CODEC
        )

        getCodecRegistry(Interaction.CODEC).register(
            HyFlaskInteraction.REQUIRE_EFFECT.id,
            FlaskEffectRequireInteraction::class.java,
            FlaskEffectRequireInteraction.CODEC
        )

        getCodecRegistry(Interaction.CODEC).register(
            HyFlaskInteraction.MODIFY_JUMP_HEIGHT.id,
            ModifyJumpHeightInteraction::class.java,
            ModifyJumpHeightInteraction.CODEC
        )

        getCodecRegistry(Interaction.CODEC).register(
            HyFlaskInteraction.HAS_USES.id,
            HasUsesInteraction::class.java,
            HasUsesInteraction.CODEC
        )

        getCodecRegistry(Interaction.CODEC).register(
            HyFlaskInteraction.MODIFY_USES.id,
            ModifyUsesInteraction::class.java,
            ModifyUsesInteraction.CODEC
        )
    }

    private fun registerPages() {
        getCodecRegistry(OpenCustomUIInteraction.PAGE_CODEC).register(
            HyFlaskPage.FLASK_EFFECT_SELECTION.id,
            FlaskEffectSelectionSupplier::class.java,
            FlaskEffectSelectionSupplier.CODEC
        )
    }
    override fun start() {
        logger.atInfo().log("[$name] Started!")
    }

    override fun shutdown() {
        logger.atInfo().log("[$name] Shut down")
        config.save()
        instance = null
    }
}
