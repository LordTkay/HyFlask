package de.lordtkay.hyflask

import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import de.lordtkay.hyflask.uses.command.GetUsesCommand
import de.lordtkay.hyflask.uses.command.UsesCommandCollection

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

        commandRegistry.registerCommand(UsesCommandCollection())

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
