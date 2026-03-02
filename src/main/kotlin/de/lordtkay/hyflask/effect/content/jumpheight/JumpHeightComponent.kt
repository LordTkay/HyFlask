package de.lordtkay.hyflask.effect.content.jumpheight

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

class JumpHeightComponent(height: Int? = null) : Component<EntityStore?> {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()

        lateinit var componentType: ComponentType<EntityStore?, JumpHeightComponent>

        val CODEC: BuilderCodec<JumpHeightComponent>

        init {
            val builder = BuilderCodec.builder(JumpHeightComponent::class.java, ::JumpHeightComponent)

            builder
                .append(
                    KeyedCodec("Height", Codec.INTEGER),
                    { component, value -> component.height = value },
                    { component -> component.height }
                )
                .documentation("The height modifier that the player has")
                .add()

            CODEC = builder.build()
        }
    }

    var height: Int = height ?: 0
        private set

    override fun clone(): Component<EntityStore?> {
        val copy = JumpHeightComponent()
        copy.height = height
        return copy
    }

}