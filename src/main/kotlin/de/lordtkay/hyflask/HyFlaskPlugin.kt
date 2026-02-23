package de.lordtkay.hyflask

import com.hypixel.hytale.assetstore.map.IndexedAssetMap
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.asset.HytaleAssetStore
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
import de.lordtkay.hyflask.effect.interaction.FlaskEffectApplyInteraction
import de.lordtkay.hyflask.effect.ui.FlaskEffectSelectionSupplier

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
        registerPages()
        commandRegistry.registerCommand(HyFlaskCommandCollection())

        logger.atInfo().log("[$name] Setup complete!")
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
            FlaskEffectComponent.ID,
            FlaskEffectComponent.CODEC
        )
        FlaskEffectComponent.componentType = flaskEffectComponent
    }

    private fun registerInteractions() {
        getCodecRegistry(Interaction.CODEC).register(
            FlaskEffectApplyInteraction.ID,
            FlaskEffectApplyInteraction::class.java,
            FlaskEffectApplyInteraction.CODEC
        )
    }

    private fun registerPages() {
        getCodecRegistry(OpenCustomUIInteraction.PAGE_CODEC).register(
            FlaskEffectSelectionSupplier.ID,
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
