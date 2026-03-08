package de.lordtkay.hyflask.uses.command

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStat.USES
import de.lordtkay.hyflask.utility.command.EntityStatUtility

class AddUsesCommand(
    parentTranslationKey: String,
    private val translationKey: String = "$parentTranslationKey.add"
) : AbstractTargetPlayerCommand("add", translationKey) {

    private val amountArg: RequiredArg<Int> = this.withRequiredArg(
        "amount",
        "$translationKey.amount",
        ArgTypes.INTEGER
    )

    override fun execute(
        commandContext: CommandContext,
        sourceRef: Ref<EntityStore?>?,
        ref: Ref<EntityStore?>,
        playerRef: PlayerRef,
        world: World,
        store: Store<EntityStore?>
    ) {
        val amount = amountArg.get(commandContext).toFloat()
        val message = when (val result = EntityStatUtility.add(ref, store, USES, amount)) {
            is EntityStatUtility.Result.ComponentMissing ->
                Message.translation("server.hyflask.commands.error")

            is EntityStatUtility.Result.Success ->
                Message.translation("$translationKey.success")
                    .param("uses", result.amount)
                    .param("max", result.max)
        }

        commandContext.sendMessage(message)
    }
}
