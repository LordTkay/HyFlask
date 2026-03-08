package de.lordtkay.hyflask.uses.command

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStat.USES

class AddUsesCommand(
    parentTranslationKey: String,
    private val translationKey: String = "$parentTranslationKey.add"
) : AbstractTargetPlayerCommand("add", translationKey) {
    companion object {
        private val logger = HytaleLogger.forEnclosingClass()
    }

    private val amountArg: RequiredArg<Int> = this.withRequiredArg(
        "amount",
        "$translationKey.amount",
        ArgTypes.INTEGER
    )

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

        val addedUses = amountArg.get(commandContext)
        val currentUses = statMap.addStatValue(statIndex, addedUses.toFloat())
        val currentMax = usesStat?.max ?: 0f

        val message = Message.translation("$translationKey.success")
            .param("uses", currentUses)
            .param("max", currentMax)
        playerRef.sendMessage(message)
    }
}