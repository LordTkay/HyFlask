package de.lordtkay.hyflask.effect.content.waterbreathing

import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.ComponentAccessor
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.math.util.ChunkUtil
import com.hypixel.hytale.math.util.MathUtil
import com.hypixel.hytale.protocol.BlockMaterial
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent
import com.hypixel.hytale.server.core.modules.collision.WorldUtil
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.Condition
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.TargetUtil
import de.lordtkay.hyflask.enumeration.HyFlaskAssetTag
import java.time.Instant

/**
 * Represents a condition that evaluates whether an entity possesses the "Water Breathing" effect.
 */
class WaterBreathingCondition(inverse: Boolean = false) : Condition(inverse) {

    companion object {
        val CODEC: BuilderCodec<WaterBreathingCondition>

        init {
            val builder = BuilderCodec.builder(
                WaterBreathingCondition::class.java,
                ::WaterBreathingCondition,
                BASE_CODEC
            )
            CODEC = builder.build()
        }
    }

    override fun eval0(
        componentAccessor: ComponentAccessor<EntityStore?>,
        ref: Ref<EntityStore?>,
        instant: Instant
    ): Boolean {

        val effectComponent = componentAccessor.getComponent(ref, EffectControllerComponent.getComponentType())
            ?: return false

        effectComponent.activeEffects.values.forEach { effect ->
            val asset = EntityEffect.getAssetMap().getAsset(effect.entityEffectIndex) ?: return@forEach
            val data = EntityEffect.CODEC.getData(asset) ?: return@forEach

            if (!HyFlaskAssetTag.WATER_BREATHING.exists(data)) return@forEach

            // The effect should only work when the player is in a drowning state and not when they suffocate
            return isNotSuffocating(componentAccessor, ref)
        }

        return false
    }

    /**
     *
     */
    private fun isNotSuffocating(
        componentAccessor: ComponentAccessor<EntityStore?>,
        ref: Ref<EntityStore?>
    ): Boolean {
        val world = componentAccessor.getExternalData().world
        val lookVec = TargetUtil.getLook(ref, componentAccessor)
        val position = lookVec.getPosition()
        val chunkStore = world.chunkStore
        val chunkIndex: Long = ChunkUtil.indexChunkFromBlock(position.x, position.z)
        val chunkRef: Ref<ChunkStore?>? = chunkStore.getChunkReference(chunkIndex)

        if (chunkRef != null && chunkRef.isValid) {
            val packed = WorldUtil.getPackedMaterialAndFluidAtPosition(
                chunkRef,
                chunkStore.store,
                position.x,
                position.y,
                position.z
            )
            val material = BlockMaterial.VALUES[MathUtil.unpackLeft(packed)]

            return material == BlockMaterial.Empty
        }

        return true
    }
}