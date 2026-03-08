package de.lordtkay.hyflask.uses.interaction

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.validation.Validators
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.protocol.InteractionState
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStat.USES

class ModifyUsesInteraction : SimpleInstantInteraction() {
    companion object {
        private var logger = HytaleLogger.forEnclosingClass()

        val CODEC: BuilderCodec<ModifyUsesInteraction>

        init {
            var builder = BuilderCodec.builder(
                ModifyUsesInteraction::class.java,
                ::ModifyUsesInteraction,
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

            CODEC = builder.build()
        }
    }

    var amount: Int = 1
        private set

    override fun firstRun(
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
        val modifiedUses = currentUses + amount
        statMap.setStatValue(statIndex, modifiedUses)

        context.state.state = InteractionState.Finished
    }
}