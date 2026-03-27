package de.lordtkay.hyflask.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec
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

            builder
                .append(
                    KeyedCodec("UsesUpgradeMap", MapCodec(Codec.INTEGER) { mutableMapOf() }),
                    { component, value -> component.usesUpgradeMap = value.mapKeys { it.key.toInt() }.toSortedMap() },
                    { component -> component.usesUpgradeMap.mapKeys { it.key.toString() } }
                )
                .documentation("A map that defines what max modifier the player received when a specific number of consumptions were made. The key defines the number of consumptions required, and the value defines the new maximum modifier.")
                .add()

            CODEC = builder.build()
        }
    }

    /**
     * Contains the IDs of [FlaskEffect] assets that a new player will start with and could equip.
     */
    var startingLearnedEffects: MutableSet<String> = mutableSetOf("FlaskEffect_HealthRegen_T1")
        private set

    /**
     * Contains the IDs of [FlaskEffect] assets that a new player will start with and will be executed, when used.
     */
    var startingActiveEffects: MutableSet<String> = mutableSetOf("FlaskEffect_HealthRegen_T1")
        private set

    /**
     * A map that defines what max modifier the player received when a specific number of consumptions were made.
     * The key defines the number of consumptions required, and the value defines the new maximum modifier.
     */
    var usesUpgradeMap = sortedMapOf(
        5 to 2,
        10 to 3,
        15 to 4,
        20 to 5
    )
}