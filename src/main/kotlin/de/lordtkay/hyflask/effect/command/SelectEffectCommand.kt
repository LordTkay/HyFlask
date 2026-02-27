package de.lordtkay.hyflask.effect.command

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.effect.component.FlaskEffectComponent
import de.lordtkay.hyflask.effect.ui.FlaskEffectSelectionPage

class SelectEffectCommand : AbstractTargetPlayerCommand("select", "server.hyflask.commands.effects.select") {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()
    }

    override fun execute(
        commandContext: CommandContext,
        ref: Ref<EntityStore?>?,
        ref1: Ref<EntityStore?>,
        playerRef: PlayerRef,
        world: World,
        store: Store<EntityStore?>
    ) {
        val player = store.ensureAndGetComponent(ref1, Player.getComponentType())
        val flaskEffectComponent = store.ensureAndGetComponent(ref1, FlaskEffectComponent.componentType)
        val entityStatMap = store.ensureAndGetComponent(ref1, EntityStatMap.getComponentType())

        player.pageManager.openCustomPage(
            ref1,
            store,
            FlaskEffectSelectionPage(playerRef, flaskEffectComponent, entityStatMap)
        )
    }
}