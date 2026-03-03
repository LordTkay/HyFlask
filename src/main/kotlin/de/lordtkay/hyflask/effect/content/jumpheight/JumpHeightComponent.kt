package de.lordtkay.hyflask.effect.content.jumpheight

import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec
import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

class JumpHeightComponent : Component<EntityStore?> {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()

        lateinit var componentType: ComponentType<EntityStore?, JumpHeightComponent>

        val CODEC: BuilderCodec<JumpHeightComponent>

        init {
            val builder = BuilderCodec.builder(JumpHeightComponent::class.java, ::JumpHeightComponent)

            builder
                .append(
                    KeyedCodec("Modifiers", MapCodec.STRING_HASH_MAP_CODEC),
                    { component, value ->
                        component.modifiers = value.mapValues { it.value.toFloat() }.toMutableMap()
                        component.changes = value.mapValues { it.value.toFloat() }.values.sum()
                    },
                    { component -> component.modifiers.mapValues { it.value.toString() } }
                )
                .documentation("The height modifiers that the player has")
                .add()

            CODEC = builder.build()
        }
    }

    private var modifiers: MutableMap<String, Float> = mutableMapOf()
    private var changes: Float = 0f

    fun addModifier(source: String, value: Float) {
        if (modifiers.containsKey(source)) return
        modifiers[source] = value
        changes += value
    }

    fun removeModifier(source: String) {
        val removedValue = modifiers.remove(source) ?: return
        changes -= removedValue
    }

    fun getAndResetChanges(): Float {
        val changes = this.changes
        this.changes = 0f
        return changes
    }

    fun getSources(): MutableSet<String> = modifiers.keys.toMutableSet()

    fun isEmpty(): Boolean = modifiers.isEmpty()

    override fun clone(): Component<EntityStore?> {
        val copy = JumpHeightComponent()
        copy.modifiers = modifiers.toMutableMap()
        copy.changes = changes
        return copy
    }

}