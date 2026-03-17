package de.lordtkay.hyflask.capacity.interaction

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.validation.Validators
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.protocol.InteractionState
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier.ModifierTarget
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStat
import de.lordtkay.hyflask.utility.EntityStatUtility

class ModifyCapacityInteraction : SimpleInstantInteraction() {
    companion object {
        private var logger = HytaleLogger.forEnclosingClass()

        val CODEC: BuilderCodec<ModifyCapacityInteraction>

        init {
            var builder = BuilderCodec.builder(
                ModifyCapacityInteraction::class.java,
                ::ModifyCapacityInteraction,
                SimpleInstantInteraction.CODEC
            )

            builder
                .append(
                    KeyedCodec("Amount", Codec.INTEGER),
                    { component, value -> component.amount = value },
                    { component -> component.amount }
                )
                .documentation("Amount by which 'Uses' gets modified. Positive values add and negative values remove 'Uses'.")
                .addValidator(Validators.nonNull())
                .add()

            builder
                .append(
                    KeyedCodec("Modifier", Codec.STRING),
                    { component, value -> component.modifierName = value },
                    { component -> component.modifierName }
                )
                .documentation("Amount by which 'Uses' gets modified. Positive values add and negative values remove 'Uses'.")
                .addValidator(Validators.nonNull())
                .add()

            CODEC = builder.build()
        }
    }

    var amount: Int = 0
        private set

    var modifierName: String = ""
        private set

    override fun firstRun(
        type: InteractionType,
        context: InteractionContext,
        cooldownHandler: CooldownHandler
    ) {
        val ref = context.entity
        val store = ref.store

        val result = EntityStatUtility.addModifier(
            ref,
            store,
            HyFlaskEntityStat.CAPACITY,
            modifierName,
            amount.toFloat(),
            ModifierTarget.MAX,
        )

        context.state.state = when (result) {
            is EntityStatUtility.Result.Success -> InteractionState.Finished
            else -> InteractionState.Failed
        }
    }
}