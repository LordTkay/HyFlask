package de.lordtkay.hyflask

import com.hypixel.hytale.assetstore.map.IndexedAssetMap
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.asset.HytaleAssetStore
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import de.lordtkay.hyflask.effect.asset.FlaskEffect

@Suppress("unused")
class HyFlaskPlugin(init: JavaPluginInit) : JavaPlugin(init) {

    companion object {
        private val logger = HytaleLogger.forEnclosingClass()
        var instance: HyFlaskPlugin? = null
            private set
    }

    init {
        instance = this
    }

    override fun setup() {
        logger.atInfo().log("[$name] Setting up...")

        registerAssetStores()

        logger.atInfo().log("[$name] Setup complete!")
    }


    private fun registerAssetStores() {
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

    override fun start() {
        logger.atInfo().log("[$name] Started!")
    }

    override fun shutdown() {
        logger.atInfo().log("[$name] Shut down")
        instance = null
    }
}
