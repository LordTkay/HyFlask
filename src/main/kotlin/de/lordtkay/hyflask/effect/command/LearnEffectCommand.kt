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
import de.lordtkay.hyflask.effect.asset.FlaskEffect
import de.lordtkay.hyflask.effect.component.FlaskEffectComponent

class LearnEffectCommand(
    parentTranslationKey: String,
    private val translationKey: String = "$parentTranslationKey.learn"
) : AbstractTargetPlayerCommand("learn", translationKey) {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()
    }

    private val effectIdArg = this.withRequiredArg(
        "effectId",
        "$translationKey.effectId",
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
            learnSingleEffect(flaskEffectComponent, effectId)
        } else {
            learnAllEffects(flaskEffectComponent)
        }


        playerRef.sendMessage(message)
    }

    private fun learnAllEffects(flaskEffectComponent: FlaskEffectComponent): Message {
        val values = FlaskEffect.assetMap.assetMap.values
        values.forEach { flaskEffectComponent.learnEffect(it.id) }
        return Message.translation("$translationKey.all.success")
            .param("count", values.size)
    }

    private fun learnSingleEffect(
        flaskEffectComponent: FlaskEffectComponent,
        effectId: String
    ): Message {
        return when (val result = flaskEffectComponent.learnEffect(effectId)) {
            is FlaskEffectComponent.LearnResult.Success ->
                Message.translation("$translationKey.success")
                    .param("name", result.asset.displayNameWithId)

            is FlaskEffectComponent.LearnResult.AlreadyLearned ->
                Message.translation("$translationKey.alreadyLearned")
                    .param("name", result.asset.displayNameWithId)

            FlaskEffectComponent.LearnResult.UnknownAsset ->
                Message.translation("server.hyflask.commands.effects.invalidEffectId")
                    .param("id", effectId)
        }
    }
}