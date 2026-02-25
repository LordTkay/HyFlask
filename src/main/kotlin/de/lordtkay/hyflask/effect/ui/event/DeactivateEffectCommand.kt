package de.lordtkay.hyflask.effect.ui.event

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import de.lordtkay.hyflask.effect.ui.FlaskEffectSelectionPage
import de.lordtkay.hyflask.utility.command.Command

class DeactivateEffectCommand(
    val commandBuilder: UICommandBuilder,
    val eventBuilder: UIEventBuilder,
    val activeGroups: MutableList<FlaskEffectSelectionPage.EffectGroup>,
    val learnedGroups: MutableList<FlaskEffectSelectionPage.EffectGroup>,
    val group: FlaskEffectSelectionPage.EffectGroup,
    var newIndex: Int = learnedGroups.size
) : Command {

    private var previousIndex: Int = -1

    override fun execute(): Boolean {
        if (group.activeEffect == null) return false

        group.activeEffect = null
        previousIndex = activeGroups.indexOf(group)

        val activeSelector = FlaskEffectSelectionPage.getActiveEffectSelector(previousIndex)
        commandBuilder.remove(activeSelector)
        activeGroups.remove(group)

        learnedGroups.add(group)

        commandBuilder.append("#LearnedEffects #Content", "Pages/FlaskEffectLearnedItem.ui")
        FlaskEffectSelectionPage.applyLearnedEffectElement(
            commandBuilder,
            eventBuilder,
            group,
            newIndex
        )
        return true
    }

    override fun undo(): Command {
        return ActivateEffectCommand(commandBuilder, eventBuilder, activeGroups, learnedGroups, group, previousIndex)
    }
}