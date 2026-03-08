package de.lordtkay.hyflask.uses.command

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStat.USES
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStatModifier.COMMAND_ADDITIVE

class ResetMaxUsesCommand : AbstractTargetPlayerCommand("resetMax", "server.hyflask.commands.uses.resetMax") {
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
        val statIndex = USES.getIndex()
        val usesStat = statMap.get(statIndex)

        val modStat = usesStat?.getModifier(COMMAND_ADDITIVE.id)
        if (modStat != null) {
            statMap.removeModifier(statIndex, COMMAND_ADDITIVE.id)
        }

        // Inform player
        val currentUses = usesStat?.get() ?: 0f
        val currentMax = usesStat?.max ?: 0f
        val message = Message.translation("server.hyflask.commands.uses.resetMax.success")
            .param("uses", currentUses)
            .param("max", currentMax)
        playerRef.sendMessage(message)
    }
}