package de.lordtkay.hyflask.effect.command

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.effect.component.FlaskEffectComponent

class ActivateEffectCommand : AbstractTargetPlayerCommand("activate", "server.hyflask.commands.effects.activate") {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()
    }

    private val effectIdArg = this.withRequiredArg(
        "effectId",
        "server.hyflask.commands.effects.activate.effectId",
        ArgTypes.STRING
    )

    override fun execute(
        commandContext: CommandContext,
        ref: Ref<EntityStore?>?,
        ref1: Ref<EntityStore?>,
        playerRef: PlayerRef,
        world: World,
        store: Store<EntityStore?>
    ) {
        val flaskEffectComponent =
            store.ensureAndGetComponent(playerRef.reference!!, FlaskEffectComponent.componentType)

        val effectId = effectIdArg.get(commandContext).uppercase()
        val assetName = fetchEffect(logger, playerRef, effectId) ?: return

        val wasActivated = flaskEffectComponent.activateEffect(effectId)

        if (!wasActivated) {
            if (flaskEffectComponent.knowsEffect(effectId)) {
                val message = Message.translation("server.hyflask.commands.effects.activate.alreadyActive")
                    .param("name", assetName)
                playerRef.sendMessage(message)

                logger.atInfo()
                    .log("Player '${playerRef.username}' tried to activate flask effect '${assetName}' but it was already active")
            } else {
                val message = Message.translation("server.hyflask.commands.effects.activate.notLearned")
                    .param("name", assetName)
                playerRef.sendMessage(message)

                logger.atInfo()
                    .log("Player '${playerRef.username}' tried to activate flask effect '${assetName}' but it was not learned yet")
            }
            return
        }

        val message = Message.translation("server.hyflask.commands.effects.activate.success")
            .param("name", assetName)
        playerRef.sendMessage(message)

        logger.atInfo().log("Player '${playerRef.username}' activated flask effect '${assetName}'")
    }
}