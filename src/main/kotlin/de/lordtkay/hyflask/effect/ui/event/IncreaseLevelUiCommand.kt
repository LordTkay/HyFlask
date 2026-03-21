package de.lordtkay.hyflask.effect.ui.event

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import de.lordtkay.hyflask.effect.ui.FlaskEffectSelectionPage
import de.lordtkay.hyflask.utility.ui.command.UiCommand

class IncreaseLevelUiCommand(
    val activeGroups: Map<String, FlaskEffectSelectionPage.EffectGroup>,
    val group: FlaskEffectSelectionPage.EffectGroup
) : UiCommand {

    override fun execute(commandBuilder: UICommandBuilder, eventBuilder: UIEventBuilder): Boolean {
        val activeEffect = group.activeEffect ?: return false

        val level = activeEffect.groupDetails?.level ?: 1
        val nextLevel = group.learnedEffects.higherEntry(level) ?: return false

        group.activeEffect = nextLevel.value
        val index = activeGroups.values.indexOf(group)

        FlaskEffectSelectionPage.applyActiveEffectElement(
            commandBuilder,
            eventBuilder,
            group,
            index
        )
        return true
    }

    override fun undo(): UiCommand {
        return DecreaseLevelUiCommand(activeGroups, group)
    }
}