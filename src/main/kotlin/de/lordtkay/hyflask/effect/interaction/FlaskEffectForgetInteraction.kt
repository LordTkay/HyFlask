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

class FlaskEffectForgetInteraction : SimpleInstantInteraction() {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()

        val CODEC: BuilderCodec<FlaskEffectForgetInteraction>

        init {
            var builder = BuilderCodec.builder(
                FlaskEffectForgetInteraction::class.java,
                ::FlaskEffectForgetInteraction,
                SimpleInstantInteraction.CODEC
            )

            builder
                .append(
                    KeyedCodec("EffectId", Codec.STRING),
                    { component, value -> component.effectId = value },
                    { component -> component.effectId }
                )
                .documentation("Effect ID that should be forgotten.")
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
                .log("No command buffer found for interaction '${FlaskEffectForgetInteraction::class.java.simpleName}'")
            return
        }
        val ref = interactionContext.entity
        val player = commandBuffer.ensureAndGetComponent(ref, Player.getComponentType())

        val effectComponent = commandBuffer.ensureAndGetComponent(ref, FlaskEffectComponent.componentType)
        interactionContext.state.state = InteractionState.Failed
        var message: Message

        when (val result = effectComponent.forgetEffect(effectId)) {
            is FlaskEffectComponent.ForgetResult.Success -> {
                message = Message.translation("server.hyflask.notification.flaskEffect.forgotten.success")
                    .param("name", result.asset.displayNameWithId)
                interactionContext.state.state = InteractionState.Finished
            }

            is FlaskEffectComponent.ForgetResult.NotLearned -> {
                message = Message.translation("server.hyflask.notification.flaskEffect.forgotten.success")
                    .param("name", result.asset.displayNameWithId)
                interactionContext.state.state = InteractionState.Finished
            }

            FlaskEffectComponent.ForgetResult.UnknownAsset -> {
                message = Message.translation("server.hyflask.notification.flaskEffect.unknown")
                    .param("name", effectId)
            }

            FlaskEffectComponent.ForgetResult.SuccessUnknownAsset -> {
                message = Message.translation("server.hyflask.notification.flaskEffect.forgotten.success")
                    .param("name", effectId)

                interactionContext.state.state = InteractionState.Finished
            }
        }

        player.sendMessage(message)
    }
}