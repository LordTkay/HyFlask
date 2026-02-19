package de.lordtkay.hyflask.effect.component

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.entity.InteractionManager
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.HyFlaskPlugin
import de.lordtkay.hyflask.effect.asset.FlaskEffect
import de.lordtkay.hyflask.effect.protocol.FlaskEffectInteractionType.Consumption

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
            learnedEffects.addAll(config.startingLearnedEffects.map { normalizeAssetId(it) })
            activeEffects.addAll(config.startingActiveEffects.map { normalizeAssetId(it) })
        } else {
            logger.atWarning().log("Could not load config for initializing flask effects")
        }
    }

    val activeEffectsDisplayNames get() = activeEffects.mapNotNull { getEffectAsset(it)?.displayNameWithId }
    val learnedEffectsDisplayNames get() = learnedEffects.mapNotNull { getEffectAsset(it)?.displayNameWithId }

    fun learnEffect(assetId: String): LearnResult {
        val normalizedAssetId = normalizeAssetId(assetId)
        val asset = getEffectAsset(normalizedAssetId) ?: return LearnResult.UnknownAsset

        if (normalizedAssetId in learnedEffects) {
            logger.atFine().log("Player already knows flask effect '${asset.displayNameWithId}'")
            return LearnResult.AlreadyLearned(asset)
        }

        logger.atFine().log("Player learned flask effect '${asset.displayNameWithId}'")
        learnedEffects.add(normalizedAssetId)
        return LearnResult.Success(asset)
    }

    fun forgetEffect(assetId: String): ForgetResult {
        val normalizedAssetId = normalizeAssetId(assetId)
        val asset = getEffectAsset(normalizedAssetId) ?: return ForgetResult.UnknownAsset

        if (normalizedAssetId !in learnedEffects) {
            logger.atFine().log("Player does not know flask effect '${asset.displayNameWithId}'")
            return ForgetResult.NotLearned(asset)
        }

        activeEffects.remove(normalizedAssetId)
        learnedEffects.remove(normalizedAssetId)
        logger.atFine().log("Player forgot flask effect '${asset.displayNameWithId}'")
        return ForgetResult.Success(asset)
    }

    fun activateEffect(assetId: String): ActivateResult {
        val normalizedAssetId = normalizeAssetId(assetId)
        val asset = getEffectAsset(normalizedAssetId) ?: return ActivateResult.UnknownAsset

        if (normalizedAssetId !in learnedEffects) {
            logger.atFine().log("Player does not know flask effect '${asset.displayNameWithId}'")
            return ActivateResult.NotLearned(asset)
        }

        if (normalizedAssetId in activeEffects) {
            logger.atFine().log("Player already has flask effect '${asset.displayNameWithId}' active")
            return ActivateResult.AlreadyActive(asset)
        }

        activeEffects.add(normalizedAssetId)
        logger.atFine().log("Player activated flask effect '${asset.displayNameWithId}'")
        return ActivateResult.Success(asset)
    }

    fun deactivateEffect(assetId: String): DeactivateResult {
        val normalizedAssetId = normalizeAssetId(assetId)
        val asset = getEffectAsset(normalizedAssetId) ?: return DeactivateResult.UnknownAsset

        if (normalizedAssetId !in activeEffects) {
            logger.atFine().log("Player does not have flask effect '${asset.displayNameWithId}' active")
            return DeactivateResult.NotActive(asset)
        }

        activeEffects.remove(normalizedAssetId)
        logger.atFine().log("Player deactivated flask effect '${asset.displayNameWithId}'")
        return DeactivateResult.Success(asset)
    }

    fun executeEffect(
        playerStoreRef: Ref<EntityStore>,
        interactionManager: InteractionManager
    ): ExecuteResult {

        val interactionContext = InteractionContext.forInteraction(
            interactionManager,
            playerStoreRef,
            InteractionType.Secondary,
            playerStoreRef.store
        )

        activeEffects.ifEmpty {
            logger.atFine().log("Player does not have any flask effects active")
            return ExecuteResult.NoActiveEffects
        }

        var successfullyExecutions = 0

        activeEffects.forEach { effectId ->
            val asset = getEffectAsset(effectId) ?: return@forEach

            val rootInteractionId = asset.interactions[Consumption]
            if (rootInteractionId == null) {
                logger.atWarning()
                    .log("Flask effect asset '${asset.displayNameWithId}' does not have a '${Consumption.name}' interaction")
                return@forEach
            }

            val rootInteraction = RootInteraction.getAssetMap().getAsset(rootInteractionId)
            if (rootInteraction == null) {
                logger.atWarning()
                    .log("Could not find root interaction with ID '$rootInteractionId' for flask effect asset '${asset.displayNameWithId}'")
                return@forEach
            }

            val effectChain = interactionManager.initChain(
                InteractionType.Secondary,
                interactionContext,
                rootInteraction,
                false
            )
            interactionManager.queueExecuteChain(effectChain)
            logger.atFine().log("Executed flask effect '${asset.displayNameWithId}'")
            successfullyExecutions++
        }

        if (successfullyExecutions == 0) {
            logger.atFine().log("No flask effects were executed")
            return ExecuteResult.NoEffectsExecuted
        }

        logger.atFine().log("Executed $successfullyExecutions/$${activeEffects.size} flask effects")
        return ExecuteResult.Success(successfullyExecutions, activeEffects.size)
    }

    private fun getEffectAsset(assetId: String): FlaskEffect? {
        val asset = FlaskEffect.assetMap.getAsset(assetId)

        if (asset == null) {
            logger.atWarning().log("Could not find flask effect asset with ID '$assetId'")
            return null
        }

        return asset
    }

    private fun normalizeAssetId(assetId: String) = assetId.uppercase()

    override fun clone(): Component<EntityStore?> {
        val copy = FlaskEffectComponent()
        copy.activeEffects = activeEffects.toMutableSet()
        copy.learnedEffects = learnedEffects.toMutableSet()
        return copy
    }

    sealed interface LearnResult {
        data object UnknownAsset : LearnResult
        data class Success(val asset: FlaskEffect) : LearnResult
        data class AlreadyLearned(val asset: FlaskEffect) : LearnResult
    }

    sealed interface ForgetResult {
        data object UnknownAsset : ForgetResult
        data class Success(val asset: FlaskEffect) : ForgetResult
        data class NotLearned(val asset: FlaskEffect) : ForgetResult
    }

    sealed interface ActivateResult {
        data object UnknownAsset : ActivateResult
        data class Success(val asset: FlaskEffect) : ActivateResult
        data class NotLearned(val asset: FlaskEffect) : ActivateResult
        data class AlreadyActive(val asset: FlaskEffect) : ActivateResult
    }

    sealed interface DeactivateResult {
        data object UnknownAsset : DeactivateResult
        data class Success(val asset: FlaskEffect) : DeactivateResult
        data class NotActive(val asset: FlaskEffect) : DeactivateResult
    }

    sealed interface ExecuteResult {
        data class Success(val successful: Int, val total: Int) : ExecuteResult
        data object NoActiveEffects : ExecuteResult
        data object NoEffectsExecuted : ExecuteResult
    }
}