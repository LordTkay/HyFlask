package de.lordtkay.hyflask.effect.component

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.HyFlaskPlugin
import de.lordtkay.hyflask.effect.asset.FlaskEffect

class FlaskEffectComponent : Component<EntityStore?> {

    companion object {
        const val ID = "HyFlask_FlaskEffect"
        private var logger = HytaleLogger.forEnclosingClass()

        lateinit var componentType: ComponentType<EntityStore?, FlaskEffectComponent>

        val CODEC: BuilderCodec<FlaskEffectComponent>

        init {
            val builder = BuilderCodec.builder(FlaskEffectComponent::class.java, ::FlaskEffectComponent)

            builder
                .append(
                    KeyedCodec("LearnedEffects", Codec.STRING_ARRAY),
                    { component, value -> component.learnedEffects.addAll(value.map { it.uppercase() }) },
                    { component -> component.learnedEffects.toTypedArray() }
                )
                .documentation("A list of effects that the player has learned and could equip.")
                .add()

            builder
                .append(
                    KeyedCodec("ActiveEffects", Codec.STRING_ARRAY),
                    { component, value -> component.activeEffects.addAll(value.map { it.uppercase() }) },
                    { component -> component.activeEffects.toTypedArray() }
                )
                .documentation("A list of effects that the player currently has active and would be executed, when consuming the flask.")
                .add()

            CODEC = builder.build()
        }
    }

    /**
     * Contains the IDs of [FlaskEffect] assets that the player has learned and could equip.
     */
    var learnedEffects: MutableSet<String> = mutableSetOf()
        private set

    /**
     * Contains the IDs of [FlaskEffect] assets that are currently active on the player and will be executed, when used.
     */
    var activeEffects: MutableSet<String> = mutableSetOf()
        private set

    init {
        val config = HyFlaskPlugin.instance?.config?.get()
        if (config != null) {
            learnedEffects.addAll(config.startingLearnedEffects.map { it.uppercase() })
            activeEffects.addAll(config.startingActiveEffects.map { it.uppercase() })
        } else {
            logger.atWarning().log("Could not load config for initializing flask effects")
        }
    }

    /**
     * Adds the specified effect to the list of learned effects if it is not already known.
     *
     * @param assetId The ID of the [FlaskEffect] asset that should be learned.
     * @return `true` if the effect was successfully learned, or `false` if the effect was already known.
     */
    fun learnEffect(assetId: String): Boolean {
        val modifiedAssetId = assetId.uppercase()
        if (knowsEffect(modifiedAssetId)) {
            logger.atFine().log("Player already knows flask effect '$modifiedAssetId'")
            return false
        }

        logger.atFine().log("Player learned flask effect '$modifiedAssetId'")
        learnedEffects.add(modifiedAssetId)
        return true
    }

    /**
     * Removes the specified effect from the list of learned effects if it exists and deactivates it if it is active.
     *
     * @param assetId The ID of the [FlaskEffect] asset that should be forgotten.
     * @return `true` if the effect was successfully forgotten, or `false` if the effect was not learned.
     */
    fun forgetEffect(assetId: String): Boolean {
        val modifiedAssetId = assetId.uppercase()
        if (!knowsEffect(modifiedAssetId)) {
            logger.atFine().log("Player does not know flask effect '$modifiedAssetId' and thus cannot forget it")
            return false
        }

        if (activatedEffect(modifiedAssetId)) {
            activeEffects.remove(modifiedAssetId)
        }

        logger.atFine().log("Player forgot flask effect '$modifiedAssetId'")
        learnedEffects.remove(modifiedAssetId)
        return true
    }

    /**
     * Activates the specified effect if it is learned and not already active.
     *
     * @param assetId The ID of the [FlaskEffect] asset that should be activated.
     * @return `true` if the effect was successfully activated, or `false` if the effect was not learned or already active.
     */
    fun activateEffect(assetId: String): Boolean {
        val modifiedAssetId = assetId.uppercase()
        if (!knowsEffect(modifiedAssetId)) {
            logger.atFine().log("Player does not know flask effect '$modifiedAssetId' and thus cannot activate it")
            return false
        }

        if (activatedEffect(modifiedAssetId)) {
            logger.atFine().log("Player already has flask effect '$modifiedAssetId' active")
            return false
        }

        logger.atFine().log("Player activated flask effect '$modifiedAssetId'")
        activeEffects.add(modifiedAssetId)
        return true
    }

    /**
     * Deactivates the specified effect if it is currently active.
     *
     * @param assetId The ID of the [FlaskEffect] asset that should be deactivated.
     * @return `true` if the effect was successfully deactivated, or `false` if the effect was not active.
     */
    fun deactivateEffect(assetId: String): Boolean {
        val modifiedAssetId = assetId.uppercase()
        if (!activatedEffect(modifiedAssetId)) {
            logger.atFine()
                .log("Player does not have flask effect '$modifiedAssetId' active and thus cannot deactivate it")
            return false
        }

        logger.atFine().log("Player deactivated flask effect '$modifiedAssetId'")
        activeEffects.remove(modifiedAssetId)
        return true
    }

    /**
     * Checks whether the specified effect is in the list of learned effects.
     *
     * @param assetId The ID of the effect asset to check.
     * @return `true` if the effect has been learned, otherwise `false`.
     */
    fun knowsEffect(assetId: String) = assetId.uppercase() in learnedEffects

    /**
     * Checks whether the specified effect is currently active.
     *
     * @param assetId The ID of the effect asset to check.
     * @return `true` if the effect is active, otherwise `false`.
     */
    fun activatedEffect(assetId: String) = assetId.uppercase() in activeEffects


    override fun clone(): Component<EntityStore?> {
        val copy = FlaskEffectComponent()
        copy.activeEffects = activeEffects.toMutableSet()
        copy.learnedEffects = learnedEffects.toMutableSet()
        return copy
    }
}