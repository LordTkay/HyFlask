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
import de.lordtkay.hyflask.utility.EntityStatUtility

class GetUsesCommand(
    parentTranslationKey: String,
    private val translationKey: String = "$parentTranslationKey.get"
) : AbstractTargetPlayerCommand("get", translationKey) {

    override fun execute(
        commandContext: CommandContext,
        sourceRef: Ref<EntityStore?>?,
        ref: Ref<EntityStore?>,
        playerRef: PlayerRef,
        world: World,
        store: Store<EntityStore?>
    ) {
        val message = when (val result = EntityStatUtility.get(ref, store, USES)) {
            is EntityStatUtility.Result.ComponentMissing ->
                Message.translation("server.hyflask.commands.error")

            is EntityStatUtility.Result.Success ->
                Message.translation("$translationKey.success")
                    .param("uses", result.current.toInt())
                    .param("max", result.max.toInt())
        }

        commandContext.sendMessage(message)
    }
}
