package de.lordtkay.hyflask.effect.content.jumpheight

import com.hypixel.hytale.assetstore.AssetRegistry
import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.tick.EntityTickingSystem
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.enumeration.HyFlaskAssetTag

private const val ONE_BLOCK_HEIGHT = 2.5f

class JumpHeightSystem : EntityTickingSystem<EntityStore>() {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()
    }

    override fun getQuery(): Query<EntityStore?> {
        return Query.and(
            PlayerRef.getComponentType(),
            EffectControllerComponent.getComponentType(),
            MovementManager.getComponentType()
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
        var jumpHeightComponent = chunk.getComponent(index, JumpHeightComponent.componentType)

        val ref = playerRef.reference!!

        var hadJumpHeightEffect = false

        effectComponent.activeEffects.values.forEach { effect ->
            val asset = EntityEffect.getAssetMap().getAsset(effect.entityEffectIndex) ?: return@forEach
            val data = EntityEffect.CODEC.getData(asset) ?: return@forEach

            // Check if the effect has the HyFlask-Tag
            val tagIndex = AssetRegistry.getTagIndex(HyFlaskAssetTag.HYFLASK_EFFECT.ids[0])
            val tag = data.getTag(tagIndex) ?: return@forEach
            if (tag.isEmpty()) return@forEach

            // Check if the effect has the Jump-Height-Tag and extract the jump height
            val jumpHeightKey =
                data.rawTags.keys.find { it.startsWith(HyFlaskAssetTag.JUMP_HEIGHT.ids[1]) } ?: return@forEach
            val split = jumpHeightKey.split("_")
            if (split.size <= 1) return@forEach
            val jumpHeight = Integer.parseInt(split[1])

            hadJumpHeightEffect = true

            if (jumpHeightComponent == null) {
                jumpHeightComponent = JumpHeightComponent(jumpHeight)
                handleApplication(commandBuffer, ref, playerRef, movementManager, jumpHeightComponent)
            }

            if (!effect.isInfinite && effect.remainingDuration <= duration) {
                handleRemoval(commandBuffer, ref, playerRef, movementManager, jumpHeightComponent)
            }
        }

        if (!hadJumpHeightEffect && jumpHeightComponent != null) {
            handleRemoval(commandBuffer, ref, playerRef, movementManager, jumpHeightComponent)
        }
    }

    private fun handleApplication(
        commandBuffer: CommandBuffer<EntityStore?>,
        ref: Ref<EntityStore?>,
        playerRef: PlayerRef,
        movementManager: MovementManager,
        jumpHeightComponent: JumpHeightComponent
    ) {
        commandBuffer.addComponent(ref, JumpHeightComponent.componentType, jumpHeightComponent)
        movementManager.settings.jumpForce += (jumpHeightComponent.height * ONE_BLOCK_HEIGHT)
        movementManager.update(playerRef.packetHandler)
        logger.atInfo()
            .log("Player applied jump height effect: +${jumpHeightComponent.height} blocks")
    }


    private fun handleRemoval(
        commandBuffer: CommandBuffer<EntityStore?>,
        ref: Ref<EntityStore?>,
        playerRef: PlayerRef,
        movementManager: MovementManager,
        jumpHeightComponent: JumpHeightComponent
    ) {
        movementManager.settings.jumpForce -= (jumpHeightComponent.height * ONE_BLOCK_HEIGHT)
        movementManager.update(playerRef.packetHandler)
        commandBuffer.removeComponent(ref, JumpHeightComponent.componentType)
        logger.atInfo()
            .log("Player removed jump height effect: -${jumpHeightComponent.height} blocks")

    }
}