package de.lordtkay.hyflask.capacity.command

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStat
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStatModifier.COMMAND_ADDITIVE

class ResetMaxCapacityCommand(
    parentTranslationKey: String,
    private val translationKey: String = "$parentTranslationKey.resetMax"
) : AbstractTargetPlayerCommand("resetMax", translationKey) {

    companion object {
        private val logger = HytaleLogger.forEnclosingClass()
    }

    override fun execute(
        commandContext: CommandContext,
        ref: Ref<EntityStore?>?,
        ref1: Ref<EntityStore?>,
        playerRef: PlayerRef,
        world: World,
        store: Store<EntityStore?>
    ) {
        val statMap = store.getComponent(playerRef.reference!!, EntityStatMap.getComponentType())
        if (statMap == null) {
            logger.atWarning()
                .log("${EntityStatMap::class.simpleName} was not found on player reference.")
            playerRef.sendMessage(Message.translation("server.hyflask.commands.error"))
            return
        }
        val assetMap = EntityStatType.getAssetMap()
        val statIndex = assetMap.getIndex(HyFlaskEntityStat.CAPACITY.id)

        statMap.removeModifier(statIndex, COMMAND_ADDITIVE.id)
        val newMaxCapacity = statMap.get(statIndex)?.max ?: 0f

        val message =
            Message.translation("$translationKey.success")
                .param("maxCapacity", newMaxCapacity)
        playerRef.sendMessage(message)

    }
}