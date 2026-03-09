package de.lordtkay.hyflask.capacity.command

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStat
import de.lordtkay.hyflask.utility.EntityStatUtility

class GetCapacityCommand(
    parentTranslationKey: String,
    private val translationKey: String = "$parentTranslationKey.get"
) : AbstractTargetPlayerCommand("get", translationKey) {

    companion object {
        private val logger = HytaleLogger.forEnclosingClass()
    }

    override fun execute(
        commandContext: CommandContext,
        sourceRef: Ref<EntityStore?>?,
        ref: Ref<EntityStore?>,
        playerRef: PlayerRef,
        world: World,
        store: Store<EntityStore?>
    ) {
        val result = EntityStatUtility.get(
            ref,
            store,
            HyFlaskEntityStat.CAPACITY,
        )

        val message = when (result) {
            is EntityStatUtility.Result.ComponentMissing ->
                Message.translation("server.hyflask.commands.error")

            is EntityStatUtility.Result.Success ->
                Message.translation("$translationKey.success")
                    .param("current", result.current)
                    .param("max", result.max)
        }

        commandContext.sendMessage(message)
    }
}