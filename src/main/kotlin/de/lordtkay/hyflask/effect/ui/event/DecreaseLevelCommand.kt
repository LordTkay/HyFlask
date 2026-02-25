package de.lordtkay.hyflask.effect.ui.event

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import de.lordtkay.hyflask.effect.ui.FlaskEffectSelectionPage
import de.lordtkay.hyflask.utility.command.Command

class DecreaseLevelCommand(
    val commandBuilder: UICommandBuilder,
    val eventBuilder: UIEventBuilder,
    val activeGroups: List<FlaskEffectSelectionPage.EffectGroup>,
    val learnedGroups: List<FlaskEffectSelectionPage.EffectGroup>,
    val group: FlaskEffectSelectionPage.EffectGroup
) : Command {

    override fun execute(): Boolean {
        val activeEffect = group.activeEffect ?: return false

        val level = activeEffect.groupDetails?.level ?: 1
        val previousLevel = group.learnedEffects.lowerEntry(level) ?: return false

        group.activeEffect = previousLevel.value
        val index = activeGroups.indexOf(group)

        FlaskEffectSelectionPage.applyActiveEffectElement(
            commandBuilder,
            eventBuilder,
            activeGroups,
            learnedGroups,
            group,
            index
        )
        return true
    }

    override fun undo(): Command {
        return IncreaseLevelCommand(commandBuilder, eventBuilder, activeGroups, learnedGroups, group)
    }
}