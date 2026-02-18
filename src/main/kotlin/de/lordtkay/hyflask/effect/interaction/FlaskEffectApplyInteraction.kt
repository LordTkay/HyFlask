package de.lordtkay.hyflask.effect.interaction

import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.protocol.InteractionState
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction
import com.hypixel.hytale.server.core.universe.PlayerRef
import de.lordtkay.hyflask.effect.asset.FlaskEffect
import de.lordtkay.hyflask.effect.component.FlaskEffectComponent
import de.lordtkay.hyflask.effect.protocol.FlaskEffectInteractionType.Consumption

class FlaskEffectApplyInteraction : SimpleInstantInteraction() {

    companion object {
        const val ID = "HyFlask_ApplyEffect"
        private var logger = HytaleLogger.forEnclosingClass()

        val CODEC: BuilderCodec<FlaskEffectApplyInteraction>

        init {
            var builder = BuilderCodec.builder(
                FlaskEffectApplyInteraction::class.java,
                ::FlaskEffectApplyInteraction,
                SimpleInstantInteraction.CODEC
            )

            CODEC = builder.build()
        }
    }

    override fun firstRun(
        interactionType: InteractionType,
        interactionContext: InteractionContext,
        cooldownHandler: CooldownHandler
    ) {
        val commandBuffer = interactionContext.commandBuffer
        if (commandBuffer == null) {
            logger.atWarning()
                .log("No command buffer found for interaction '${FlaskEffectApplyInteraction::class.java.simpleName}'")
            return
        }
        val interactionManager = interactionContext.interactionManager

        val playerStoreRef = interactionContext.entity
        val effectData = commandBuffer.ensureAndGetComponent(playerStoreRef, FlaskEffectComponent.componentType)
        val playerRef = commandBuffer.ensureAndGetComponent(playerStoreRef, PlayerRef.getComponentType())

        effectData.activeEffects.ifEmpty {
            logger.atFine().log("Player '${playerRef.username}' does not have any flask effects active")
            return
        }

        var successfullyExecutions = 0

        effectData.activeEffects.forEach { effectId ->
            val effectAsset = FlaskEffect.assetMap.getAsset(effectId)

            if (effectAsset == null) {
                logger.atSevere().log("Could not find flask effect asset with ID '$effectId'")
                return@forEach
            }

            val rootInteractionId = effectAsset.interactions[Consumption]
            if (rootInteractionId == null) {
                logger.atSevere()
                    .log("Flask effect asset '$effectId' does not have a '${Consumption.name}' interaction")
                return@forEach
            }

            val rootInteraction = RootInteraction.getAssetMap().getAsset(rootInteractionId)
            if (rootInteraction == null) {
                logger.atSevere()
                    .log("Could not find root interaction with ID '$rootInteractionId' for flask effect asset '$effectId'")
                return@forEach
            }

            val context = InteractionContext.forInteraction(
                interactionManager,
                playerStoreRef,
                InteractionType.Secondary,
                playerStoreRef.store
            )
            val effectChain = interactionManager.initChain(InteractionType.Secondary, context, rootInteraction, false)
            interactionManager.queueExecuteChain(effectChain)

            logger.atFine().log("Player '${playerRef.username}' applied flask effect '$effectId'")

            successfullyExecutions++
        }

        interactionContext.state.state =
            if (successfullyExecutions > 0) InteractionState.Finished else InteractionState.Failed
    }
}