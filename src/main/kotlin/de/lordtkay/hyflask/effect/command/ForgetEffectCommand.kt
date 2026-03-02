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

class ForgetEffectCommand : AbstractTargetPlayerCommand("forget", "server.hyflask.commands.effects.forget") {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()
    }

    private val effectIdArg = this.withRequiredArg(
        "effectId",
        "server.hyflask.commands.effects.forget.effectId",
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

        val message: Message = if (effectId.uppercase() != "ALL") {
            forgetSingleEffect(flaskEffectComponent, effectId)
        } else {
            forgetAllEffects(flaskEffectComponent)
        }

        playerRef.sendMessage(message)
    }

    private fun forgetAllEffects(flaskEffectComponent: FlaskEffectComponent): Message {
        val learnedEffects = flaskEffectComponent.learnedEffects.toList()
        learnedEffects.forEach { flaskEffectComponent.forgetEffect(it) }
        return Message.translation("server.hyflask.commands.effects.forget.all.success")
            .param("count", learnedEffects.size)
    }

    private fun forgetSingleEffect(
        flaskEffectComponent: FlaskEffectComponent,
        effectId: String
    ): Message = when (val result = flaskEffectComponent.forgetEffect(effectId)) {
        is FlaskEffectComponent.ForgetResult.Success ->
            Message.translation("server.hyflask.commands.effects.forget.success")
                .param("name", result.asset.displayNameWithId)

        is FlaskEffectComponent.ForgetResult.NotLearned ->
            Message.translation("server.hyflask.commands.effects.forget.notLearned")
                .param("name", result.asset.displayNameWithId)

        FlaskEffectComponent.ForgetResult.SuccessUnknownAsset ->
            Message.translation("server.hyflask.commands.effects.forget.success")
                .param("name", effectId)

        FlaskEffectComponent.ForgetResult.UnknownAsset ->
            Message.translation("server.hyflask.commands.effects.invalidEffectId")
                .param("id", effectId)
    }
}