package de.lordtkay.hyflask.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import de.lordtkay.hyflask.effect.asset.FlaskEffect

class FlaskConfig {

    companion object {
        const val CONFIG_NAME = "config"
        val CODEC: BuilderCodec<FlaskConfig>

        init {
            val builder = BuilderCodec.builder(FlaskConfig::class.java, ::FlaskConfig)

            builder
                .append(
                    KeyedCodec("StartingLearnedEffects", Codec.STRING_ARRAY),
                    { component, value -> component.startingLearnedEffects.addAll(value) },
                    { component -> component.startingLearnedEffects.toTypedArray() }
                )
                .documentation("A list of learned effects that a new player will start with and could equip.")
                .add()

            // TODO: Find a way to do a cross validation, to check if the active effects are inside of the learned effects
            builder
                .append(
                    KeyedCodec("StartingActiveEffects", Codec.STRING_ARRAY),
                    { component, value -> component.startingActiveEffects.addAll(value) },
                    { component -> component.startingActiveEffects.toTypedArray() }
                )
                .documentation("A list of active effects that a new player will start with and will be executed, when used.")
                .add()

            CODEC = builder.build()
        }
    }

    /**
     * Contains the IDs of [FlaskEffect] assets that a new player will start with and could equip.
     */
    var startingLearnedEffects: MutableSet<String> = mutableSetOf()
        private set

    /**
     * Contains the IDs of [FlaskEffect] assets that a new player will start with and will be executed, when used.
     */
    var startingActiveEffects: MutableSet<String> = mutableSetOf()
        private set

}