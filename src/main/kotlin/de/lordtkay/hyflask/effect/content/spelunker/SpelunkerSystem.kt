package de.lordtkay.hyflask.effect.content.spelunker

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap
import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.tick.EntityTickingSystem
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.protocol.UpdateType
import com.hypixel.hytale.protocol.packets.assets.UpdateBlockTypes
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

class SpelunkerSystem : EntityTickingSystem<EntityStore>() {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()
    }

    override fun getQuery(): Query<EntityStore?> {
        return Query.and(
            PlayerRef.getComponentType(),
            EffectControllerComponent.getComponentType(),
            SpelunkerComponent.componentType
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
        val spelunkerComponent = chunk.getComponent(index, SpelunkerComponent.componentType)!!

        val ref = playerRef.reference!!

        removeInactiveEffects(spelunkerComponent, effectComponent)
        applyChanges(spelunkerComponent, playerRef)

        if (spelunkerComponent.isEmpty()) {
            commandBuffer.removeComponent(ref, SpelunkerComponent.componentType)
        }
    }


    /**
     * Applies block modification changes specified in the `SpelunkerComponent` to the player's environment
     * and sends the updates to the player.
     *
     * This method processes the changes accumulated in the given `SpelunkerComponent`, modifies the necessary
     * blocks, and sends a packet to update the block states for the player via the `PlayerRef` object. 
     * Additionally, it logs the updates performed for debugging purposes.
     *
     * @param spelunkerComponent The component containing block modifications to be applied.
     * @param playerRef A reference to the player to whom the modifications should be sent.
     */
    private fun applyChanges(
        spelunkerComponent: SpelunkerComponent,
        playerRef: PlayerRef
    ) {
        val assetMap = BlockType.getAssetMap()
        val blockChanges = mutableMapOf<Int, com.hypixel.hytale.protocol.BlockType>()

        val changes = spelunkerComponent.getAndResetChanges()

        // This contains all the IDs of all blocks that were modified since the last update. Blocks that will get a 
        // modification or where the modification was removed.
        val targetBlockIds = changes.flatMap { it.value }.toSet()
        val unmodifiedBlockIds = targetBlockIds.toMutableSet()

        // Iterate through all active modifications and generate update packages for blocks that have changed 
        // (either modified or had their modification removed). When multiple effects modify the same block, 
        // all active modifications must be reapplied or otherwise their modifications are lost.
        spelunkerComponent.blockModifications.values.forEach { modifications ->
            modifications.forEach { modification ->

                val updates = mutableSetOf<String>()
                modification.blockIds.forEach { blockId ->
                    if (!targetBlockIds.contains(blockId)) return@forEach
                    updates.add(blockId)
                }

                updates.forEach { blockId ->
                    modifyBlock(assetMap, blockId, blockChanges, modification)
                    unmodifiedBlockIds.remove(blockId)
                }
            }
        }

        // All blocks that don't have any active modification will be reset in here.
        unmodifiedBlockIds.forEach { blockId ->
            modifyBlock(assetMap, blockId, blockChanges)
        }

        if (blockChanges.isEmpty()) return

        val packet = UpdateBlockTypes(
            UpdateType.AddOrUpdate,
            assetMap.nextIndex,
            blockChanges,
            false,
            false,
            true,
            false
        )

        playerRef.packetHandler.writePacket(packet, false)

        val modifiedBlockString = targetBlockIds.joinToString(", ") {
            assetMap.getAsset(it)?.id.toString()
        }

        logger.atInfo()
            .log("${blockChanges.size} block updates are being sent to the player with the following targets: $modifiedBlockString")
    }

    private fun modifyBlock(
        assetMap: BlockTypeAssetMap<String, BlockType>,
        blockId: String,
        blockChanges: MutableMap<Int, com.hypixel.hytale.protocol.BlockType>,
        modification: SpelunkerBlockModification? = null
    ) {
        val blockIndex = assetMap.getIndex(blockId)
        val block = assetMap.getAsset(blockIndex)
        if (block == null) {
            logger.atWarning().log("Could not find block with ID '${blockId}'")
            return
        }

        val modifiedBlock = blockChanges.getOrDefault(blockIndex, block.toPacket().clone())

        modification?.light?.let { modifiedBlock.light = it }
        modification?.opacity?.let { modifiedBlock.opacity = it }
        modification?.particleSystems?.let { particles ->
            modifiedBlock.particles = particles.map {
                val modelParticle = ModelParticle()
                modelParticle.systemId = it
                modelParticle.toPacket()
            }.toTypedArray()
        }

        blockChanges[blockIndex] = modifiedBlock
    }

    /**
     * Removes inactive effects from the provided `spelunkerComponent` by comparing its data
     * with the active effects in the `effectComponent`. Any modification source in the 
     * `spelunkerComponent` that corresponds to an inactive effect in the `effectComponent` 
     * will be removed.
     *
     * @param spelunkerComponent The `SpelunkerComponent` containing the block modifications and sources to evaluate.
     * @param effectComponent The `EffectControllerComponent` containing the currently active effects.
     */
    private fun removeInactiveEffects(
        spelunkerComponent: SpelunkerComponent,
        effectComponent: EffectControllerComponent
    ) {
        val sources = spelunkerComponent.getSources()

        effectComponent.activeEffects.values.forEach { effect ->
            val asset = EntityEffect.getAssetMap().getAsset(effect.entityEffectIndex) ?: return@forEach
            sources.remove(asset.id)
        }

        sources.forEach { assetId -> spelunkerComponent.removeModification(assetId) }
    }

}