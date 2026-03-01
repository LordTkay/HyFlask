package de.lordtkay.hyflask.effect.content.recall

import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.tick.EntityTickingSystem
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.enumeration.HyFlaskAssetTag

class RecallSystem : EntityTickingSystem<EntityStore>() {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()
    }

    override fun getQuery(): Query<EntityStore?> {
        return Query.and(
            PlayerRef.getComponentType(),
            EffectControllerComponent.getComponentType(),
            TransformComponent.getComponentType()
        )
    }

    override fun tick(
        duration: Float,
        index: Int,
        chunk: ArchetypeChunk<EntityStore?>,
        store: Store<EntityStore?>,
        commandBuffer: CommandBuffer<EntityStore?>
    ) {
        val effectComponent = chunk.getComponent(index, EffectControllerComponent.getComponentType())!!
        val playerRef = chunk.getComponent(index, PlayerRef.getComponentType())!!
        var recallComponent = chunk.getComponent(index, RecallComponent.componentType)

        val ref = playerRef.reference!!

        var hadRecallEffect = false

        effectComponent.activeEffects.values.forEach { effect ->
            val asset = EntityEffect.getAssetMap().getAsset(effect.entityEffectIndex) ?: return@forEach
            val data = EntityEffect.CODEC.getData(asset) ?: return@forEach
            if (!HyFlaskAssetTag.RECALL.exists(data)) return@forEach

            hadRecallEffect = true
            val transformComponent = chunk.getComponent(index, TransformComponent.getComponentType())!!

            if (recallComponent == null) {
                logger.atFine().log("Player started a recall action")
                recallComponent = RecallComponent(transformComponent.position)
                commandBuffer.addComponent(
                    ref,
                    RecallComponent.componentType,
                    recallComponent
                )
            }

            val distance = recallComponent.position.distanceTo(transformComponent.position)
            val maximumDistanceInBlocks = 1

            if (distance > maximumDistanceInBlocks) {
                // Player moved too far and the effect will be removed
                logger.atFine().log("Player moved too far for the recall and thereby will be canceled")
                effectComponent.removeEffect(ref, effect.entityEffectIndex, store)
                commandBuffer.removeComponent(ref, RecallComponent.componentType)
            }

            if (!effect.isInfinite && effect.getRemainingDuration() <= duration) {
                logger.atFine().log("Player will be recalled")
                handleRecall(store, ref, commandBuffer)
            }
        }

        if (!hadRecallEffect && recallComponent != null) {
            commandBuffer.removeComponent(ref, RecallComponent.componentType)
        }
    }

    private fun handleRecall(
        store: Store<EntityStore?>,
        ref: Ref<EntityStore?>,
        commandBuffer: CommandBuffer<EntityStore?>
    ) {
        val world = store.externalData.world
        Player.getRespawnPosition(
            ref,
            world.name,
            store
        ).thenAcceptAsync({
            val teleportComponent = Teleport.createForPlayer(it)
            commandBuffer.addComponent(ref, Teleport.getComponentType(), teleportComponent)
            commandBuffer.removeComponent(ref, RecallComponent.componentType)
        }, world)
    }
}