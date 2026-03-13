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

        removeInactiveEffects(jumpHeightComponent, effectComponent)
        applyChanges(jumpHeightComponent, movementManager, playerRef)

        if (jumpHeightComponent.isEmpty()) {
            commandBuffer.removeComponent(ref, JumpHeightComponent.componentType)
        }
    }

    /**
     * Updates the movement settings related to jump height based on the changes recorded
     * in the provided `JumpHeightComponent`. If changes are detected, the movement manager
     * is updated and a log entry is created for the applied modifications.
     *
     * @param jumpHeightComponent the component managing jump height modifiers for the entity.
     * @param movementManager the manager responsible for handling movement-related settings.
     * @param playerRef a reference to the player entity for which the changes are applied.
     */
    private fun applyChanges(
        jumpHeightComponent: JumpHeightComponent,
        movementManager: MovementManager,
        playerRef: PlayerRef
    ) {
        val changes = jumpHeightComponent.getAndResetChanges()
        if (changes == 0f) return

        movementManager.settings.jumpForce += changes
        movementManager.update(playerRef.packetHandler)
        logger.atInfo().log("Jump height modifier of $changes applied to player")
    }

    /**
     * Removes inactive jump height effects from the provided `JumpHeightComponent` by checking
     * active effects in the `EffectControllerComponent`. Any effect listed in the
     * jump height component but no longer active will be removed.
     *
     * @param jumpHeightComponent the component managing jump height modifiers for an entity.
     * @param effectComponent the component managing active effects of an entity.
     */
    private fun removeInactiveEffects(
        jumpHeightComponent: JumpHeightComponent,
        effectComponent: EffectControllerComponent
    ) {
        // Gets all effects that are still listed
        val sources = jumpHeightComponent.getSources()

        // Removes all effects that are still active and thus are active
        effectComponent.activeEffects.values.forEach { effect ->
            val asset = EntityEffect.getAssetMap().getAsset(effect.entityEffectIndex) ?: return@forEach
            sources.remove(asset.id)
        }

        // Removes all effects that are no longer active
        sources.forEach { assetId -> jumpHeightComponent.removeModifier(assetId) }
    }
}