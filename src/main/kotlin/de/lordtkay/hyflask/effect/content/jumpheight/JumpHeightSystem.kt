package de.lordtkay.hyflask.effect.content.jumpheight

import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.tick.EntityTickingSystem
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

class JumpHeightSystem : EntityTickingSystem<EntityStore>() {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()
    }

    override fun getQuery(): Query<EntityStore?> {
        return Query.and(
            PlayerRef.getComponentType(),
            EffectControllerComponent.getComponentType(),
            MovementManager.getComponentType(),
            JumpHeightComponent.componentType
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
        val movementManager = chunk.getComponent(index, MovementManager.getComponentType())!!
        val jumpHeightComponent = chunk.getComponent(index, JumpHeightComponent.componentType)!!

        val ref = playerRef.reference!!
        val sources = jumpHeightComponent.getSources()

        effectComponent.activeEffects.values.forEach { effect ->
            val asset = EntityEffect.getAssetMap().getAsset(effect.entityEffectIndex) ?: return@forEach
            sources.remove(asset.id)
        }

        sources.forEach { assetId -> jumpHeightComponent.removeModifier(assetId) }

        val changes = jumpHeightComponent.getAndResetChanges()
        if (changes != 0f) {
            applyModifier(movementManager, playerRef, changes)
        }

        if (jumpHeightComponent.isEmpty()) {
            commandBuffer.removeComponent(ref, JumpHeightComponent.componentType)
        }
    }

    private fun applyModifier(movementManager: MovementManager, playerRef: PlayerRef, value: Float) {
        movementManager.settings.jumpForce += value
        movementManager.update(playerRef.packetHandler)
        logger.atInfo().log("Player was added jump height modifier of $value")
    }
}