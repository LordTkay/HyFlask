package de.lordtkay.hyflask.effect.interaction

import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.protocol.InteractionState
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction
import de.lordtkay.hyflask.effect.component.FlaskEffectComponent

class FlaskEffectApplyInteraction : SimpleInstantInteraction() {

    companion object {
        const val ID = "HyFlask_ApplyEffect"
        private var logger = HytaleLogger.forEnclosingClass()

        val CODEC: BuilderCodec<FlaskEffectApplyInteraction>

        init {
            var builder = BuilderCodec.builder(
                FlaskEffectApplyInteraction::class.java,
                ::FlaskEffectApplyInteraction,
                SimpleInstantInteraction.CODEC
            )

            CODEC = builder.build()
        }
    }

    override fun firstRun(
        interactionType: InteractionType,
        interactionContext: InteractionContext,
        cooldownHandler: CooldownHandler
    ) {
        val commandBuffer = interactionContext.commandBuffer
        if (commandBuffer == null) {
            logger.atWarning()
                .log("No command buffer found for interaction '${FlaskEffectApplyInteraction::class.java.simpleName}'")
            return
        }
        val interactionManager = interactionContext.interactionManager

        val playerStoreRef = interactionContext.entity
        val effectData = commandBuffer.ensureAndGetComponent(playerStoreRef, FlaskEffectComponent.componentType)

        interactionContext.state.state =
            when (val result = effectData.executeEffect(playerStoreRef, interactionManager)) {
                is FlaskEffectComponent.ExecuteResult.Success -> InteractionState.Finished
                else -> InteractionState.Failed
            }
    }
}