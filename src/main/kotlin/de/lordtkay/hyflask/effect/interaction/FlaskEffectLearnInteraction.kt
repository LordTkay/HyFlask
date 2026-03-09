package de.lordtkay.hyflask.effect.interaction

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.validation.Validators
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.protocol.InteractionState
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction
import de.lordtkay.hyflask.effect.component.FlaskEffectComponent

class FlaskEffectLearnInteraction : SimpleInstantInteraction() {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()

        val CODEC: BuilderCodec<FlaskEffectLearnInteraction>

        init {
            val builder = BuilderCodec.builder(
                FlaskEffectLearnInteraction::class.java,
                ::FlaskEffectLearnInteraction,
                SimpleInstantInteraction.CODEC
            )

            builder
                .append(
                    KeyedCodec("EffectId", Codec.STRING),
                    { component, value -> component.effectId = value },
                    { component -> component.effectId }
                )
                .documentation("Effect ID that should be learned.")
                .addValidator(Validators.nonNull())
                .add()

            CODEC = builder.build()
        }
    }

    var effectId: String = ""
        private set


    override fun firstRun(
        interactionType: InteractionType,
        interactionContext: InteractionContext,
        cooldownHandler: CooldownHandler
    ) {
        interactionContext.state.state = InteractionState.Failed
        val commandBuffer = interactionContext.commandBuffer
        if (commandBuffer == null) {
            logger.atWarning()
                .log("No command buffer found for interaction '${FlaskEffectLearnInteraction::class.java.simpleName}'")
            return
        }
        val ref = interactionContext.entity
        val player = commandBuffer.ensureAndGetComponent(ref, Player.getComponentType())

        val effectComponent = commandBuffer.ensureAndGetComponent(ref, FlaskEffectComponent.componentType)
        interactionContext.state.state = InteractionState.Failed
        var message: Message

        when (val result = effectComponent.learnEffect(effectId)) {
            is FlaskEffectComponent.LearnResult.Success -> {
                message = Message.translation("server.hyflask.notification.flaskEffect.learned.success")
                    .param("name", result.asset.displayName)
                interactionContext.state.state = InteractionState.Finished
            }

            is FlaskEffectComponent.LearnResult.AlreadyLearned -> {
                message = Message.translation("server.hyflask.notification.flaskEffect.learned.alreadyKnown")
                    .param("name", result.asset.displayName)
            }

            FlaskEffectComponent.LearnResult.UnknownAsset -> {
                message = Message.translation("server.hyflask.notification.flaskEffect.unknown")
                    .param("name", effectId)
            }
        }

        player.sendMessage(message)
    }
}