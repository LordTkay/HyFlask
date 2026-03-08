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

class ActivateEffectCommand(
    private val parentTranslationKey: String,
    private val translationKey: String = "$parentTranslationKey.activate"
) : AbstractTargetPlayerCommand("activate", translationKey) {

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
        sourceRef: Ref<EntityStore?>?,
        ref: Ref<EntityStore?>,
        playerRef: PlayerRef,
        world: World,
        store: Store<EntityStore?>
    ) {
        val flaskEffectComponent =
            store.ensureAndGetComponent(ref, FlaskEffectComponent.componentType)

        val effectId = effectIdArg.get(commandContext)

        val message = when (val result = flaskEffectComponent.activateEffect(effectId, ref, store)) {
            is FlaskEffectComponent.ActivateResult.Success ->
                Message.translation("$translationKey.success")
                    .param("name", result.asset.displayNameWithId)

            is FlaskEffectComponent.ActivateResult.AlreadyActive ->
                Message.translation("$translationKey.alreadyActive")
                    .param("name", result.asset.displayNameWithId)

            is FlaskEffectComponent.ActivateResult.NotLearned ->
                Message.translation("$translationKey.notLearned")
                    .param("name", result.asset.displayNameWithId)

            FlaskEffectComponent.ActivateResult.UnknownAsset ->
                Message.translation("$parentTranslationKey.invalidEffectId")
                    .param("id", effectId)
        }

        commandContext.sendMessage(message)
    }
}