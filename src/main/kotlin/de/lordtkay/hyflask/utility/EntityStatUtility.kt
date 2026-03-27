package de.lordtkay.hyflask.utility

import com.hypixel.hytale.component.ComponentAccessor
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStat
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStatModifier

/**
 * Utility object for handling operations related to entity statistics.
 *
 * Provides methods to retrieve, modify, and apply modifiers to specific statistic values
 * associated with entities in a given store. It supports operations such as adding values,
 * setting exact values, and managing modifiers for entity stats.
 */
object EntityStatUtility {

    private val logger = HytaleLogger.forEnclosingClass()

    fun get(
        ref: Ref<EntityStore?>,
        store: ComponentAccessor<EntityStore?>,
        targetStat: HyFlaskEntityStat
    ): Result {
        val statContext = getStatContext(ref, store, targetStat)
            ?: return Result.ComponentMissing(EntityStatMap::class.java)

        return Result.Success(statContext.current, statContext.min, statContext.max)
    }

    fun add(
        ref: Ref<EntityStore?>,
        store: ComponentAccessor<EntityStore?>,
        targetStat: HyFlaskEntityStat,
        value: Float
    ): Result {
        val statContext = getStatContext(ref, store, targetStat)
            ?: return Result.ComponentMissing(EntityStatMap::class.java)

        val modifiedAmount = statContext.current + value
        statContext.statMap.setStatValue(statContext.index, modifiedAmount)
        return Result.Success(statContext.current, statContext.min, statContext.max)
    }

    fun set(
        ref: Ref<EntityStore?>,
        store: ComponentAccessor<EntityStore?>,
        targetStat: HyFlaskEntityStat,
        value: Float
    ): Result {
        val statContext = getStatContext(ref, store, targetStat)
            ?: return Result.ComponentMissing(EntityStatMap::class.java)

        statContext.statMap.setStatValue(statContext.index, value)
        return Result.Success(statContext.current, statContext.min, statContext.max)
    }

    fun addModifier(
        ref: Ref<EntityStore?>,
        store: ComponentAccessor<EntityStore?>,
        targetStat: HyFlaskEntityStat,
        modifier: HyFlaskEntityStatModifier,
        value: Float,
        modifierTarget: Modifier.ModifierTarget,
        modifierType: StaticModifier.CalculationType = StaticModifier.CalculationType.ADDITIVE
    ): Result {
        return addModifier(ref, store, targetStat, modifier.id, value, modifierTarget, modifierType)
    }

    fun addModifier(
        ref: Ref<EntityStore?>,
        store: ComponentAccessor<EntityStore?>,
        targetStat: HyFlaskEntityStat,
        modifier: String,
        value: Float,
        modifierTarget: Modifier.ModifierTarget,
        modifierType: StaticModifier.CalculationType = StaticModifier.CalculationType.ADDITIVE
    ): Result {
        val statContext = getStatContext(ref, store, targetStat)
            ?: return Result.ComponentMissing(EntityStatMap::class.java)

        val existingModifier = statContext.statMap.getModifier(statContext.index, modifier)

        var modifierAmount = value
        if (existingModifier is StaticModifier) {
            modifierAmount += existingModifier.amount
        }

        val modifiedModifier = StaticModifier(
            modifierTarget,
            modifierType,
            modifierAmount
        )

        statContext.statMap.putModifier(statContext.index, modifier, modifiedModifier)
        return Result.Success(statContext.current, statContext.min, statContext.max)
    }

    /**
     * Calculates the modifier amount required to make the player's total Min or Max value match the desired value.
     */
    fun setToTotal(
        ref: Ref<EntityStore?>,
        store: ComponentAccessor<EntityStore?>,
        targetStat: HyFlaskEntityStat,
        modifier: HyFlaskEntityStatModifier,
        value: Float,
        modifierTarget: Modifier.ModifierTarget
    ): Result {
        return setToTotal(ref, store, targetStat, modifier.id, value, modifierTarget)
    }

    /**
     * Calculates the modifier amount required to make the player's total Min or Max value match the desired value.
     */
    fun setToTotal(
        ref: Ref<EntityStore?>,
        store: ComponentAccessor<EntityStore?>,
        targetStat: HyFlaskEntityStat,
        modifier: String,
        value: Float,
        modifierTarget: Modifier.ModifierTarget
    ): Result {
        val statContext = getStatContext(ref, store, targetStat)
            ?: return Result.ComponentMissing(EntityStatMap::class.java)

        var modifiedMax = statContext.max
        val existingModifier = statContext.statMap.getModifier(statContext.index, modifier)
        if (existingModifier is StaticModifier) {
            modifiedMax -= existingModifier.amount
        }
        val modifierAmount = value - modifiedMax

        val modifiedModifier = StaticModifier(
            modifierTarget,
            StaticModifier.CalculationType.ADDITIVE,
            modifierAmount
        )

        statContext.statMap.putModifier(statContext.index, modifier, modifiedModifier)
        return Result.Success(statContext.current, statContext.min, statContext.max)
    }

    fun getModifier(
        ref: Ref<EntityStore?>,
        store: ComponentAccessor<EntityStore?>,
        targetStat: HyFlaskEntityStat,
        modifier: HyFlaskEntityStatModifier
    ): GetModifierResult {
        return getModifier(ref, store, targetStat, modifier.id)
    }

    fun getModifier(
        ref: Ref<EntityStore?>,
        store: ComponentAccessor<EntityStore?>,
        targetStat: HyFlaskEntityStat,
        modifier: String
    ): GetModifierResult {
        val statContext = getStatContext(ref, store, targetStat)
            ?: return GetModifierResult.ComponentMissing(EntityStatMap::class.java)

        val existingModifier = statContext.statMap.getModifier(statContext.index, modifier)

        if (existingModifier !is StaticModifier) return GetModifierResult.ModifierMissing
        return GetModifierResult.Success(
            existingModifier.amount,
            existingModifier.calculationType,
            existingModifier.target
        )
    }

    fun reset(
        ref: Ref<EntityStore?>,
        store: ComponentAccessor<EntityStore?>,
        targetStat: HyFlaskEntityStat
    ): Result {
        val statContext = getStatContext(ref, store, targetStat)
            ?: return Result.ComponentMissing(EntityStatMap::class.java)

        val newValue = statContext.statMap.resetStatValue(statContext.index)
        return Result.Success(newValue, statContext.min, statContext.max)
    }

    fun removeModifier(
        ref: Ref<EntityStore?>,
        store: ComponentAccessor<EntityStore?>,
        targetStat: HyFlaskEntityStat,
        modifier: HyFlaskEntityStatModifier
    ): Result {
        return removeModifier(ref, store, targetStat, modifier.id)
    }

    fun removeModifier(
        ref: Ref<EntityStore?>,
        store: ComponentAccessor<EntityStore?>,
        targetStat: HyFlaskEntityStat,
        modifier: String,
    ): Result {
        val statContext = getStatContext(ref, store, targetStat)
            ?: return Result.ComponentMissing(EntityStatMap::class.java)

        statContext.statMap.removeModifier(statContext.index, modifier)
        return Result.Success(statContext.current, statContext.min, statContext.max)
    }

    private fun getStatContext(
        ref: Ref<EntityStore?>,
        store: ComponentAccessor<EntityStore?>,
        targetStat: HyFlaskEntityStat
    ): StatContext? {
        val statMap = store.getComponent(ref, EntityStatMap.getComponentType())
        if (statMap == null) {
            logger.atWarning()
                .log("${EntityStatMap::class.simpleName} was not found on player reference.")
            return null
        }

        val statIndex = targetStat.getIndex()

        return StatContext(
            statIndex,
            statMap
        )
    }

    private data class StatContext(
        val index: Int,
        val statMap: EntityStatMap
    ) {
        private val stat: EntityStatValue?
            get() = statMap.get(index)

        val min: Float
            get() = stat?.min ?: 0f
        val max: Float
            get() = stat?.max ?: 0f
        val current: Float
            get() = stat?.get() ?: 0f
    }

    sealed interface Result {
        data class Success(val current: Float, val min: Float, val max: Float) : Result
        data class ComponentMissing(val component: Class<*>) : Result
    }

    sealed interface GetModifierResult {
        data class Success(
            val modifier: Float,
            val calculationType: StaticModifier.CalculationType,
            val target: Modifier.ModifierTarget
        ) : GetModifierResult

        data class ComponentMissing(val component: Class<*>) : GetModifierResult
        data object ModifierMissing : GetModifierResult
    }

}
