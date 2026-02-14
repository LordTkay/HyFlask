package de.lordtkay.hyflask

import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import de.lordtkay.hyflask.command.HyFlaskCommandCollection

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

        commandRegistry.registerCommand(HyFlaskCommandCollection())

        logger.atInfo().log("[$name] Setup complete!")
    }

    override fun start() {
        logger.atInfo().log("[$name] Started!")
    }

    override fun shutdown() {
        logger.atInfo().log("[$name] Shut down")
        instance = null
    }
}
