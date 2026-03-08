package de.lordtkay.hyflask.effect.command

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.effect.component.FlaskEffectComponent

class GetEffectCommand(
    parentTranslationKey: String,
    private val translationKey: String = "$parentTranslationKey.get"
) : AbstractTargetPlayerCommand("get", translationKey) {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()
    }

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

        val activeEffects = flaskEffectComponent.activeEffectsDisplayNames
        val learnedEffects = flaskEffectComponent.learnedEffectsDisplayNames

        val message = Message.translation("$translationKey.success")
            .param("activeEffects", activeEffects.joinToString("\n") { "- $it" })
            .param("learnedEffects", learnedEffects.joinToString("\n") { "- $it" })

        playerRef.sendMessage(message)
    }
}