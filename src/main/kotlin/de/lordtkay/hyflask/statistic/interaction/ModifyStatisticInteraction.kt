package de.lordtkay.hyflask.statistic.interaction

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.EnumCodec
import com.hypixel.hytale.codec.validation.Validators
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.protocol.InteractionState
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction
import de.lordtkay.hyflask.effect.interaction.FlaskEffectForgetInteraction
import de.lordtkay.hyflask.statistic.component.FlaskStatisticComponent

class ModifyStatisticInteraction : SimpleInstantInteraction() {
    companion object {
        private var logger = HytaleLogger.forEnclosingClass()

        val CODEC: BuilderCodec<ModifyStatisticInteraction>

        init {
            var builder = BuilderCodec.builder(
                ModifyStatisticInteraction::class.java,
                ::ModifyStatisticInteraction,
                SimpleInteraction.CODEC
            )

            builder
                .append(
                    KeyedCodec("Amount", Codec.INTEGER),
                    { component, value -> component.amount = value },
                    { component -> component.amount }
                )
                .documentation("The amount that should be added to the statistic. Negative values will remove the amount.")
                .add()

            builder
                .append(
                    KeyedCodec("Target", EnumCodec(FlaskStatisticComponent.Type::class.java)),
                    { component, value -> component.type = value },
                    { component -> component.type }
                )
                .documentation("The type of statistic that should be modified.")
                .addValidator(Validators.nonNull())
                .add()

            CODEC = builder.build()
        }
    }

    var type: FlaskStatisticComponent.Type = FlaskStatisticComponent.Type.USES
        private set

    var amount: Int = 1
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
        val flaskStatisticComponent =
            commandBuffer.ensureAndGetComponent(ref, FlaskStatisticComponent.componentType).clone()
        flaskStatisticComponent.add(type, amount)
        commandBuffer.replaceComponent(ref, FlaskStatisticComponent.componentType, flaskStatisticComponent)

        interactionContext.state.state = InteractionState.Finished
    }

}