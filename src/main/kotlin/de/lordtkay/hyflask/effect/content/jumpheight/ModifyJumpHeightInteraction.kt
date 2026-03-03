package de.lordtkay.hyflask.effect.content.jumpheight

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.validation.Validators
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction

class ModifyJumpHeightInteraction : SimpleInstantInteraction() {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()

        val CODEC: BuilderCodec<ModifyJumpHeightInteraction>

        init {
            var builder = BuilderCodec.builder(
                ModifyJumpHeightInteraction::class.java,
                ::ModifyJumpHeightInteraction,
                SimpleInstantInteraction.CODEC
            )

            builder
                .append(
                    KeyedCodec("JumpHeight", Codec.FLOAT),
                    { component, value -> component.jumpHeight = value },
                    { component -> component.jumpHeight }
                )
                .documentation("The jump height that should be added to the players current jump height.")
                .addValidator(Validators.nonNull())
                .addValidator(Validators.min(0f))
                .add()

            builder
                .append(
                    KeyedCodec("RemoveOnEffectId", Codec.STRING),
                    { component, value -> component.removeOnEffectId = value },
                    { component -> component.removeOnEffectId }
                )
                .documentation("When defined, the height will be removed when the defined effect is removed.")
                .addValidator(Validators.nonNull())
                .add()


            CODEC = builder.build()
        }
    }

    var jumpHeight: Float = 0f
        private set

    var removeOnEffectId: String? = null
        private set

    override fun firstRun(
        interactionType: InteractionType,
        interactionContext: InteractionContext,
        cooldownHandler: CooldownHandler
    ) {
        val commandBuffer = interactionContext.commandBuffer
        if (commandBuffer == null) {
            logger.atWarning()
                .log("No command buffer found for interaction '${ModifyJumpHeightInteraction::class.java.simpleName}'")
            return
        }

        val ref = interactionContext.entity
        val jumpHeightComponent = commandBuffer.ensureAndGetComponent(ref, JumpHeightComponent.componentType)

        val source = removeOnEffectId ?: id
        jumpHeightComponent.addModifier(source, jumpHeight)
    }
}