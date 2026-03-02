package de.lordtkay.hyflask.effect.component

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentAccessor
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.entity.InteractionManager
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.HyFlaskPlugin
import de.lordtkay.hyflask.effect.asset.FlaskEffect
import de.lordtkay.hyflask.effect.protocol.FlaskEffectInteractionType.Consumption
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStat

class FlaskEffectComponent : Component<EntityStore?> {

    companion object {
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

    /**
     * Attempts to learn the flask effect associated with the given asset ID.
     * If the effect is already learned, a corresponding result is returned.
     *
     * @param assetId The ID of the flask effect to learn. This ID is normalized internally before processing.
     * @return A [LearnResult] indicating the outcome:
     *         - [LearnResult.UnknownAsset]: If the effect asset could not be found.
     *         - [LearnResult.AlreadyLearned]: If the player has already learned the specified effect.
     *         - [LearnResult.Success]: If the effect was successfully learned.
     */
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

    /**
     * Removes the learned flask effect associated with the given asset ID, if it exists.
     *
     * @param assetId The ID of the flask effect to forget. This ID is normalized internally before processing.
     * @return A [ForgetResult] indicating the outcome:
     *         - [ForgetResult.UnknownAsset]: If the effect asset could not be found.
     *         - [ForgetResult.NotLearned]: If the player has not learned the specified effect.
     *         - [ForgetResult.Success]: If the effect was successfully forgotten.
     */
    fun forgetEffect(assetId: String): ForgetResult {
        val normalizedAssetId = normalizeAssetId(assetId)
        val asset = getEffectAsset(normalizedAssetId)

        val activeRemoved = activeEffects.remove(normalizedAssetId)
        val learnedRemoved = learnedEffects.remove(normalizedAssetId)

        if (!activeRemoved && !learnedRemoved) {
            if (asset == null) return ForgetResult.UnknownAsset

            logger.atFine().log("Player does not know flask effect '${asset.displayNameWithId}'")
            return ForgetResult.NotLearned(asset)
        } else if (asset == null) {
            logger.atFine().log("Player forgot unknown flask effect '${normalizedAssetId}'")
            return ForgetResult.SuccessUnknownAsset
        }

        logger.atFine().log("Player forgot flask effect '${asset.displayNameWithId}'")
        return ForgetResult.Success(asset)
    }

    /**
     * Activates the flask effect associated with the specified asset ID, if possible.
     *
     * @param assetId The ID of the flask effect to activate.
     *                This ID is normalized internally before processing.
     * @return An [ActivateResult] indicating the outcome:
     *         - [ActivateResult.UnknownAsset]: If the effect asset could not be found.
     *         - [ActivateResult.NotLearned]: If the player has not learned the specified effect.
     *         - [ActivateResult.AlreadyActive]: If the effect is already active.
     *         - [ActivateResult.Success]: If the effect was successfully activated.
     */
    fun activateEffect(
        assetId: String,
        ref: Ref<EntityStore?>,
        componentAccessor: ComponentAccessor<EntityStore?>
    ): ActivateResult {
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
        applyCapacity(ref, componentAccessor)
        return ActivateResult.Success(asset)
    }

    /**
     * Deactivates the active flask effect associated with the given asset ID.
     *
     * @param assetId The ID of the flask effect to deactivate.
     *                This ID is normalized internally before processing.
     * @return A [DeactivateResult] indicating the outcome:
     *         - [DeactivateResult.UnknownAsset] if the effect asset could not be found.
     *         - [DeactivateResult.NotActive] if the effect is not currently active.
     *         - [DeactivateResult.Success] if the effect was successfully deactivated.
     */
    fun deactivateEffect(
        assetId: String,
        ref: Ref<EntityStore?>,
        componentAccessor: ComponentAccessor<EntityStore?>
    ): DeactivateResult {
        val normalizedAssetId = normalizeAssetId(assetId)
        val asset = getEffectAsset(normalizedAssetId) ?: return DeactivateResult.UnknownAsset

        if (normalizedAssetId !in activeEffects) {
            logger.atFine().log("Player does not have flask effect '${asset.displayNameWithId}' active")
            return DeactivateResult.NotActive(asset)
        }

        activeEffects.remove(normalizedAssetId)
        logger.atFine().log("Player deactivated flask effect '${asset.displayNameWithId}'")
        applyCapacity(ref, componentAccessor)
        return DeactivateResult.Success(asset)
    }

    fun deactivateAllEffect(ref: Ref<EntityStore?>, componentAccessor: ComponentAccessor<EntityStore?>) {
        activeEffects.clear()
        logger.atFine().log("Player deactivated all flask effect")
        applyCapacity(ref, componentAccessor)
    }

    private fun applyCapacity(ref: Ref<EntityStore?>, componentAccessor: ComponentAccessor<EntityStore?>) {
        val entityStatMap = componentAccessor.ensureAndGetComponent(ref, EntityStatMap.getComponentType())
        val capacityIndex = HyFlaskEntityStat.CAPACITY.getIndex()

        val capacityStat = entityStatMap.get(capacityIndex)
        if (capacityStat == null) {
            logger.atWarning()
                .log("Could not find entity stat ${HyFlaskEntityStat.CAPACITY.id} with Index $capacityIndex")
            return
        }

        val totalCost = activeEffects.mapNotNull { getEffectAsset(it) }.sumOf { it.cost }
        entityStatMap.setStatValue(capacityIndex, totalCost.toFloat())
    }

    /**
     * Executes all the active flask effects associated with the player.
     * If a flask effect is successfully executed, it will trigger the corresponding interactions.
     *
     * @param playerStoreRef A reference to the player's entity store, used to retrieve and manage player-related data.
     * @param interactionManager The interaction manager responsible for handling and executing effect interactions.
     * @return An instance of [ExecuteResult], which indicates the outcome of the operation:
     *         - [ExecuteResult.Success]: If one or more effects were successfully executed, containing the count of successfully executed effects and the total effects.
     *         - [ExecuteResult.NoActiveEffects]: If there are no active flask effects to execute.
     *         - [ExecuteResult.NoEffectsExecuted]: If the execution process fails for all active effects.
     */
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

        logger.atFine().log("Executed $successfullyExecutions/${activeEffects.size} flask effects")
        return ExecuteResult.Success(successfullyExecutions, activeEffects.size)
    }

    /**
     * Checks whether the flask effect associated with the given asset ID is currently active.
     *
     * @param assetId The ID of the flask effect to check. This ID is normalized internally before processing.
     * @return True if the flask effect is currently active, false otherwise.
     */
    fun isActive(assetId: String): Boolean {
        return normalizeAssetId(assetId) in activeEffects
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
        data object SuccessUnknownAsset : ForgetResult
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