package de.lordtkay.hyflask.uses.interaction

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.validation.Validators
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.protocol.InteractionState
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.protocol.WaitForDataFrom
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStat.USES

class HasUsesInteraction : SimpleInteraction() {
    companion object {
        private var logger = HytaleLogger.forEnclosingClass()

        val CODEC: BuilderCodec<HasUsesInteraction>

        init {
            var builder = BuilderCodec.builder(
                HasUsesInteraction::class.java,
                ::HasUsesInteraction,
                SimpleInteraction.CODEC
            )

            builder
                .append(
                    KeyedCodec("Costs", Codec.INTEGER),
                    { component, value -> component.costs = value },
                    { component -> component.costs }
                )
                .documentation("Available uses needed to pass the condition.")
                .addValidator(Validators.min(1))
                .add()
            CODEC = builder.build()
        }
    }

    var costs: Int = 1
        private set

    override fun tick0(
        firstRun: Boolean,
        time: Float,
        type: InteractionType,
        context: InteractionContext,
        cooldownHandler: CooldownHandler
    ) {
        val ref = context.entity
        val store = context.owningEntity
        val statMap = store.store.getComponent(ref, EntityStatMap.getComponentType())
        if (statMap == null) {
            logger.atWarning()
                .log("${EntityStatMap::class.simpleName} was not found on player reference.")
            return
        }
        val statIndex = USES.getIndex()
        val usesStat = statMap.get(statIndex)
        val currentUses = usesStat?.get() ?: 0f

        val state = context.state
        if (currentUses >= costs) {
            state.state = InteractionState.Finished
        } else {
            state.state = InteractionState.Failed
        }
        super.tick0(firstRun, time, type, context, cooldownHandler)
    }

    override fun getWaitForDataFrom(): WaitForDataFrom {
        return WaitForDataFrom.Server
    }
}