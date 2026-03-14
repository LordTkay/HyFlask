package de.lordtkay.hyflask.effect.content.spelunker

import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec
import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

class SpelunkerComponent : Component<EntityStore?> {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()

        lateinit var componentType: ComponentType<EntityStore?, SpelunkerComponent>

        val CODEC: BuilderCodec<SpelunkerComponent>

        init {
            val builder = BuilderCodec.builder(SpelunkerComponent::class.java, ::SpelunkerComponent)

            builder
                .append(
                    KeyedCodec("BlockModifications", MapCodec(SpelunkerBlockModification.ARRAY_CODEC, ::HashMap)),
                    { component, value ->
                        component.blockModifications = value.mapValues { it.value.toList() }.toMutableMap()
                        component.changes =
                            value.mapValues { list -> list.value.flatMap { it.blockIds }.toSet() }.toMutableMap()
                    },
                    { component -> component.blockModifications.mapValues { it.value.toTypedArray() } }
                )
                .documentation("Block modifications that should apply, when the effect is active")
                .add()

            CODEC = builder.build()
        }
    }

    var blockModifications = mutableMapOf<String, List<SpelunkerBlockModification>>()
        private set

    /**
     * Holds the information on which modifications need an update on the player. The boolean value
     * defines whether the modification should be applied or removed.
     */
    var changes = mutableMapOf<String, Set<String>>()
        private set

    fun addModification(source: String, modifications: List<SpelunkerBlockModification>) {
        blockModifications[source] = modifications
        changes[source] = modifications.flatMap { it.blockIds }.toSet()
    }

    fun removeModification(source: String) {
        val modifications = this.blockModifications.remove(source)
        changes[source] = modifications?.flatMap { it.blockIds }?.toSet() ?: emptySet()
    }

    fun getAndResetChanges(): MutableMap<String, Set<String>> {
        val changes = this.changes.toMutableMap()
        this.changes.clear()
        return changes
    }

    fun getSources(): MutableSet<String> = blockModifications.keys.toMutableSet()

    fun isEmpty() = blockModifications.isEmpty()

    override fun clone(): Component<EntityStore?> {
        val copy = SpelunkerComponent()
        copy.blockModifications = blockModifications.toMutableMap()
        copy.changes = changes.toMutableMap()
        return copy
    }
}