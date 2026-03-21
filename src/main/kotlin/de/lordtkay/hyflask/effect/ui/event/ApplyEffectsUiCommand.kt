package de.lordtkay.hyflask.effect.ui.event

import com.hypixel.hytale.component.ComponentAccessor
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.effect.component.FlaskEffectComponent
import de.lordtkay.hyflask.utility.ui.command.UiCommand

class ApplyEffectsUiCommand(
    val ref: Ref<EntityStore?>,
    val componentAccessor: ComponentAccessor<EntityStore?>,
    val flaskEffectComponent: FlaskEffectComponent,
    val activeEffects: Set<String>
) : UiCommand {

    var previousActiveEffects: Set<String>? = null

    override fun execute(commandBuilder: UICommandBuilder, eventBuilder: UIEventBuilder): Boolean {
        previousActiveEffects = flaskEffectComponent.activeEffects.toSet()
        flaskEffectComponent.deactivateAllEffect(ref, componentAccessor)

        activeEffects.forEach { effect ->
            flaskEffectComponent.activateEffect(effect, ref, componentAccessor)
        }

        return true
    }

    override fun undo(): UiCommand? {
        val previous = previousActiveEffects ?: return null
        return ApplyEffectsUiCommand(ref, componentAccessor, flaskEffectComponent, previous)
    }
}