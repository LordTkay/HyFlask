package de.lordtkay.hyflask.effect.content.spelunker

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.validation.Validators
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction

class SpelunkerInteraction : SimpleInstantInteraction() {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()

        val CODEC: BuilderCodec<SpelunkerInteraction>

        init {
            val builder = BuilderCodec.builder(
                SpelunkerInteraction::class.java,
                ::SpelunkerInteraction,
                SimpleInstantInteraction.CODEC
            )

            builder
                .append(
                    KeyedCodec("BlockModifications", SpelunkerBlockModification.ARRAY_CODEC),
                    { component, value -> component.blockModifications = value.toList() },
                    { component -> component.blockModifications.toTypedArray() }
                )
                .documentation("Block modifications that should apply, when the effect is active")
                .add()


            builder
                .append(
                    KeyedCodec("RemoveOnEffectId", Codec.STRING),
                    { component, value -> component.removeOnEffectId = value },
                    { component -> component.removeOnEffectId }
                )
                .documentation("When defined, the modifications will be removed when the defined effect is removed.")
                .addValidator(Validators.nonNull())
                .add()


            CODEC = builder.build()
        }
    }

    private var blockModifications = listOf<SpelunkerBlockModification>()
    private var removeOnEffectId: String? = null

    override fun firstRun(
        interactionType: InteractionType,
        interactionContext: InteractionContext,
        cooldownHandler: CooldownHandler
    ) {
        val commandBuffer = interactionContext.commandBuffer
        if (commandBuffer == null) {
            logger.atWarning()
                .log("No command buffer found for interaction '${SpelunkerInteraction::class.java.simpleName}'")
            return
        }

        val ref = interactionContext.entity
        commandBuffer.run { store ->
            val spelunkerComponent = store.ensureAndGetComponent(ref, SpelunkerComponent.componentType)
            val source = removeOnEffectId ?: id
            spelunkerComponent.addModification(source, blockModifications)
        }
    }
}