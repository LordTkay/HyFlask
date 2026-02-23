package de.lordtkay.hyflask.effect.asset

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.validation.Validators

class FlaskEffectGroupDetails {

    companion object {
        val CODEC: BuilderCodec<FlaskEffectGroupDetails>

        init {
            val builder = BuilderCodec.builder(
                FlaskEffectGroupDetails::class.java,
                ::FlaskEffectGroupDetails
            )

            builder
                .append(
                    KeyedCodec("GroupId", Codec.STRING),
                    { group, value -> group.id = value },
                    { group -> group.id }
                )
                .documentation("This effect will be grouped together with other effects that share the same ID.")
                .addValidator(FlaskEffectGroup.VALIDATOR_CACHE.getValidator().late())
                .addValidator(Validators.nonNull())
                .add()

            builder
                .append(
                    KeyedCodec("Level", Codec.INTEGER),
                    { group, value -> group.level = value },
                    { group -> group.level }
                )
                .documentation("The level of this effect family. Level 1 is the weakest level and they need to go up in order.")
                .addValidator(Validators.nonNull())
                .add()

            CODEC = builder.build()
        }
    }

    var id: String = ""
        private set

    var level: Int = 0
        private set
}