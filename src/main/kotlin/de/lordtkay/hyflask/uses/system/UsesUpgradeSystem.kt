package de.lordtkay.hyflask.uses.system

import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSleep
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSomnolence
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.dependency.Dependency
import com.hypixel.hytale.component.dependency.Order
import com.hypixel.hytale.component.dependency.SystemDependency
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.RefChangeSystem
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.NotificationUtil
import de.lordtkay.hyflask.HyFlaskPlugin
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStat.USES
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStatModifier.USAGE_ADDITIVE
import de.lordtkay.hyflask.statistic.component.FlaskStatisticComponent
import de.lordtkay.hyflask.statistic.component.FlaskStatisticComponent.Type
import de.lordtkay.hyflask.utility.EntityStatUtility

class UsesUpgradeSystem : RefChangeSystem<EntityStore, PlayerSomnolence>() {

    companion object {
        private val logger = HytaleLogger.forEnclosingClass()
    }


    override fun componentType(): ComponentType<EntityStore?, PlayerSomnolence?> {
        return PlayerSomnolence.getComponentType()
    }


    override fun getQuery(): Query<EntityStore?> {
        return Query.and(
            PlayerRef.getComponentType(),
            PlayerSomnolence.getComponentType(),
            FlaskStatisticComponent.componentType
        )
    }


    override fun onComponentAdded(
        ref: Ref<EntityStore?>,
        somnolenceComponent: PlayerSomnolence,
        store: Store<EntityStore?>,
        commandBuffer: CommandBuffer<EntityStore?>
    ) {
        // NOP
    }

    override fun onComponentSet(
        ref: Ref<EntityStore?>,
        oldSomnolenceComponent: PlayerSomnolence?,
        newSomnolenceComponent: PlayerSomnolence,
        store: Store<EntityStore?>,
        commandBuffer: CommandBuffer<EntityStore?>
    ) {

        if (newSomnolenceComponent.sleepState is PlayerSleep.MorningWakeUp) {
            upgradeUses(ref, commandBuffer)
        }
    }

    override fun onComponentRemoved(
        ref: Ref<EntityStore?>,
        somnolenceComponent: PlayerSomnolence,
        store: Store<EntityStore?>,
        commandBuffer: CommandBuffer<EntityStore?>
    ) {
        // NOP
    }

    private fun upgradeUses(
        ref: Ref<EntityStore?>,
        commandBuffer: CommandBuffer<EntityStore?>
    ) {
        val statisticComponent = commandBuffer.getComponent(ref, FlaskStatisticComponent.componentType)
            ?: return

        val count = statisticComponent.get(Type.USES)

        val config = HyFlaskPlugin.instance?.config?.get() ?: return
        val modified = config.usesUpgradeMap.entries.reversed().firstOrNull { count >= it.key }?.value ?: 0

        val getResult = EntityStatUtility.getModifier(
            ref,
            commandBuffer,
            USES,
            USAGE_ADDITIVE
        )

        if (getResult is EntityStatUtility.GetModifierResult.Success &&
            getResult.modifier == modified.toFloat()
        ) {
            // The modifier has not changed
            return
        }

        val setResult = EntityStatUtility.setModifier(
            ref,
            commandBuffer,
            USES,
            USAGE_ADDITIVE,
            modified.toFloat(),
            Modifier.ModifierTarget.MAX
        )

        if (setResult !is EntityStatUtility.Result.Success) {
            logger.atWarning().log("Failed to set max uses for player!")
            return
        }

        val playerRef = commandBuffer.ensureAndGetComponent(ref, PlayerRef.getComponentType())
        val statUses = EntityStatUtility.get(ref, commandBuffer, USES)

        if (statUses !is EntityStatUtility.Result.Success) {
            logger.atWarning().log("Failed to fetch current max uses for player!")
            return
        }

        val upgradeMessage = Message.translation("server.hyflask.notification.uses.upgrade.success")
            .param("uses", statUses.max.toInt())
        NotificationUtil.sendNotification(playerRef.packetHandler, upgradeMessage)

    }

    override fun getDependencies(): Set<Dependency<EntityStore?>?> {
        return setOf(
            SystemDependency(Order.BEFORE, EntityStatsModule.PlayerRegenerateStatsSystem::class.java)
        )
    }

}