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

class DeactivateEffectCommand :
    AbstractTargetPlayerCommand("deactivate", "server.hyflask.commands.effects.deactivate") {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()
    }

    private val effectIdArg = this.withRequiredArg(
        "effectId",
        "server.hyflask.commands.effects.deactivate.effectId",
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

        val effectId = effectIdArg.get(commandContext)

        val message = when (val result = flaskEffectComponent.deactivateEffect(effectId)) {
            is FlaskEffectComponent.DeactivateResult.Success ->
                Message.translation("server.hyflask.commands.effects.deactivate.success")
                    .param("name", result.asset.displayNameWithId)

            is FlaskEffectComponent.DeactivateResult.NotActive ->
                Message.translation("server.hyflask.commands.effects.deactivate.alreadyDeactivated")
                    .param("name", result.asset.displayNameWithId)

            FlaskEffectComponent.DeactivateResult.UnknownAsset ->
                Message.translation("server.hyflask.commands.effects.invalidEffectId")
                    .param("id", effectId)
        }

        playerRef.sendMessage(message)
    }
}