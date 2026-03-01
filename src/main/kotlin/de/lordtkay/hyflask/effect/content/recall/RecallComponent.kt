package de.lordtkay.hyflask.effect.content.recall

import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

class RecallComponent(position: Vector3d? = null) : Component<EntityStore?> {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()

        lateinit var componentType: ComponentType<EntityStore?, RecallComponent>

        val CODEC: BuilderCodec<RecallComponent>

        init {
            val builder = BuilderCodec.builder(RecallComponent::class.java, ::RecallComponent)

            builder
                .append(
                    KeyedCodec("StartingPosition", Vector3d.CODEC),
                    { component, value -> component.position.assign(value) },
                    { component -> component.position }
                )
                .documentation("The position of the player, when they started charging up the recall")
                .add()

            CODEC = builder.build()
        }
    }

    var position: Vector3d = position?.clone() ?: Vector3d()
        private set

    override fun clone(): Component<EntityStore?> {
        val copy = RecallComponent()
        copy.position = position.clone()
        return copy
    }

}