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
import de.lordtkay.hyflask.effect.asset.FlaskEffect
import de.lordtkay.hyflask.effect.component.FlaskEffectComponent

class FlaskEffectRequireInteraction : SimpleInstantInteraction() {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()

        val CODEC: BuilderCodec<FlaskEffectRequireInteraction>

        init {
            val builder = BuilderCodec.builder(
                FlaskEffectRequireInteraction::class.java,
                ::FlaskEffectRequireInteraction,
                SimpleInstantInteraction.CODEC
            )

            builder
                .append(
                    KeyedCodec("EffectId", Codec.STRING),
                    { component, value -> component.effectId = value },
                    { component -> component.effectId }
                )
                .documentation("Effect ID that has to be known to continue the chain.")
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
                .log("No command buffer found for interaction '${FlaskEffectRequireInteraction::class.java.simpleName}'")
            return
        }
        val ref = interactionContext.entity
        val player = commandBuffer.ensureAndGetComponent(ref, Player.getComponentType())
        val effectComponent = commandBuffer.ensureAndGetComponent(ref, FlaskEffectComponent.componentType)


        if (!effectComponent.isKnown(effectId)) {
            val name = FlaskEffect.assetMap.getAsset(effectId)?.displayName
                ?: effectId

            val message = Message.translation("server.hyflask.notification.flaskEffect.required")
                .param("name", name)
            player.sendMessage(message)
            return
        }

        interactionContext.state.state = InteractionState.Finished
    }
}