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

class SelectEffectCommand(
    parentTranslationKey: String,
    translationKey: String = "$parentTranslationKey.select"
) : AbstractTargetPlayerCommand("select", translationKey) {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()
    }

    override fun execute(
        commandContext: CommandContext,
        sourceRef: Ref<EntityStore?>?,
        ref: Ref<EntityStore?>,
        playerRef: PlayerRef,
        world: World,
        store: Store<EntityStore?>
    ) {
        val player = store.ensureAndGetComponent(ref, Player.getComponentType())
        val flaskEffectComponent = store.ensureAndGetComponent(ref, FlaskEffectComponent.componentType)
        val entityStatMap = store.ensureAndGetComponent(ref, EntityStatMap.getComponentType())

        player.pageManager.openCustomPage(
            ref,
            store,
            FlaskEffectSelectionPage(playerRef, flaskEffectComponent, entityStatMap)
        )
    }
}