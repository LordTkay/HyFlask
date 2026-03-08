package de.lordtkay.hyflask.uses.command

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStat.USES
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStatModifier.COMMAND_ADDITIVE
import de.lordtkay.hyflask.utility.command.EntityStatUtility

class ResetMaxUsesCommand(
    parentTranslationKey: String,
    private val translationKey: String = "$parentTranslationKey.resetMax"
) : AbstractTargetPlayerCommand("resetMax", translationKey) {

    override fun execute(
        commandContext: CommandContext,
        sourceRef: Ref<EntityStore?>?,
        ref: Ref<EntityStore?>,
        playerRef: PlayerRef,
        world: World,
        store: Store<EntityStore?>
    ) {
        val result = EntityStatUtility.removeModifier(ref, store, USES, COMMAND_ADDITIVE)

        val message = when (result) {
            is EntityStatUtility.Result.ComponentMissing ->
                Message.translation("server.hyflask.commands.error")

            is EntityStatUtility.Result.Success -> {
                val state = EntityStatUtility.get(ref, store, USES) as EntityStatUtility.Result.Success
                Message.translation("$translationKey.success")
                    .param("uses", state.amount)
                    .param("max", state.max)
            }
        }

        commandContext.sendMessage(message)
    }
}
