package de.lordtkay.hyflask.effect.protocol

import com.hypixel.hytale.protocol.io.ProtocolException

/**
 * These are the interaction types of the flask effect, so when the flask is consumed and the effect should be applied.
 */
enum class FlaskEffectInteractionType(private val value: Int) {
    /**
     * The interaction that is executed when the player drinks his flask
     */
    Consumption(0);

    fun getValue(): Int = value

    companion object {
        val VALUES = entries.toTypedArray()

        fun fromValue(value: Int): FlaskEffectInteractionType {
            if (value >= 0 && value < VALUES.size) {
                return VALUES[value]
            } else {
                throw ProtocolException.invalidEnumValue("InteractionType", value)
            }
        }
    }
}
