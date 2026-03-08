package de.lordtkay.hyflask.capacity.command

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier.ModifierTarget
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier.CalculationType
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStat
import de.lordtkay.hyflask.enumeration.HyFlaskEntityStatModifier.COMMAND_ADDITIVE

class AddMaxCapacityCommand(
    parentTranslationKey: String,
    private val translationKey: String = "$parentTranslationKey.addMax"
) : AbstractTargetPlayerCommand("addMax", translationKey) {

    companion object {
        private val logger = HytaleLogger.forEnclosingClass()
    }

    private val amountArg = this.withRequiredArg(
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
        val statMap = store.getComponent(ref, EntityStatMap.getComponentType())

        if (statMap == null) {
            logger.atWarning()
                .log("${EntityStatMap::class.simpleName} was not found on player reference.")
            commandContext.sendMessage(Message.translation("server.hyflask.commands.error"))
            return
        }
        val assetMap = EntityStatType.getAssetMap()
        val statIndex = assetMap.getIndex(HyFlaskEntityStat.CAPACITY.id)

        var amount = amountArg.get(commandContext).toFloat()

        val existingModifier = statMap.getModifier(statIndex, COMMAND_ADDITIVE.id) as StaticModifier?
        existingModifier?.let {
            amount += it.amount
        }

        val newModifier = StaticModifier(
            ModifierTarget.MAX,
            CalculationType.ADDITIVE,
            amount
        )

        statMap.putModifier(statIndex, COMMAND_ADDITIVE.id, newModifier)
        val newMaxCapacity = statMap.get(statIndex)?.max ?: 0f

        val message =
            Message.translation("$translationKey.success").param("maxCapacity", newMaxCapacity)
        commandContext.sendMessage(message)

    }
}