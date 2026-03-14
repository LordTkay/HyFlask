package de.lordtkay.hyflask.effect.content.spelunker

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.EnumCodec
import com.hypixel.hytale.codec.codecs.array.ArrayCodec
import com.hypixel.hytale.codec.schema.metadata.ui.UIRebuildCaches
import com.hypixel.hytale.protocol.ColorLight
import com.hypixel.hytale.protocol.Opacity
import com.hypixel.hytale.server.core.asset.type.item.config.Item
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem
import com.hypixel.hytale.server.core.codec.ProtocolCodecs

class SpelunkerBlockModification {

    companion object {
        val CODEC: BuilderCodec<SpelunkerBlockModification>
        val ARRAY_CODEC: ArrayCodec<SpelunkerBlockModification>

        init {
            val builder = BuilderCodec.builder(SpelunkerBlockModification::class.java, ::SpelunkerBlockModification)

            builder
                .appendInherited(
                    KeyedCodec("BlockIds", Codec.STRING_ARRAY),
                    { component, value -> component.blockIds = value.toList() },
                    { component -> component.blockIds.toTypedArray() },
                    { component, parent -> component.blockIds = parent.blockIds.toList() }
                )
                .addValidator(Item.VALIDATOR_CACHE.arrayValidator)
                .documentation("The block that will that will be modified.")
                .add()

            builder
                .appendInherited(
                    KeyedCodec("Opacity", EnumCodec(Opacity::class.java)),
                    { component, value -> component.opacity = value },
                    { component -> component.opacity },
                    { component, parent -> component.opacity = parent.opacity }
                )
                .documentation("Will overwrite the opacity of the blocks when the effect is active.")
                .add()

            builder
                .appendInherited(
                    KeyedCodec("Light", ProtocolCodecs.COLOR_LIGHT),
                    { component, value -> component.light = value },
                    { component -> component.light },
                    { component, parent -> component.light = parent.light }
                )
                .metadata(UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
                .documentation("Will overwrite the light of the blocks when the effect is active.")
                .add()

            builder
                .appendInherited(
                    KeyedCodec("Particles", ArrayCodec(Codec.STRING) { size -> arrayOfNulls<String>(size) }),
                    { component, value -> component.particleSystems = value.toList() },
                    { component -> component.particleSystems?.toTypedArray() },
                    { component, parent -> component.particleSystems = parent.particleSystems?.toList() }
                )
                .addValidator(ParticleSystem.VALIDATOR_CACHE.arrayValidator.late())
                .metadata(UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
                .documentation("Will overwrite the particles of the blocks when the effect is active.")
                .add()

            CODEC = builder.build()
            ARRAY_CODEC = ArrayCodec(CODEC) { size -> arrayOfNulls<SpelunkerBlockModification>(size) }
        }
    }

    var blockIds: List<String> = listOf()
        private set

    var opacity: Opacity? = null
        private set

    var light: ColorLight? = null
        private set

    var particleSystems: List<String>? = null
        private set
}