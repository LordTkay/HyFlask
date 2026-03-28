package de.lordtkay.hyflask.uses.interaction

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.validation.Validators
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.protocol.InteractionState
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.protocol.WaitForDataFrom
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.util.NotificationUtil
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
        val commandBuffer = context.commandBuffer
        if (commandBuffer == null) {
            logger.atWarning()
                .log("No command buffer found for interaction '${HasUsesInteraction::class.java.simpleName}'")
            return
        }

        val ref = context.entity
        val statMap = commandBuffer.getComponent(ref, EntityStatMap.getComponentType())
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
            val playerRef = commandBuffer.ensureAndGetComponent(ref, PlayerRef.getComponentType())
            val message = Message.translation("server.hyflask.notification.uses.has.failed")
            NotificationUtil.sendNotification(playerRef.packetHandler, message, NotificationStyle.Warning)
            state.state = InteractionState.Failed
        }
        super.tick0(firstRun, time, type, context, cooldownHandler)
    }

    override fun getWaitForDataFrom(): WaitForDataFrom {
        return WaitForDataFrom.Server
    }
}