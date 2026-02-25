package de.lordtkay.hyflask.effect.ui.event

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import de.lordtkay.hyflask.effect.ui.FlaskEffectSelectionPage
import de.lordtkay.hyflask.utility.ui.command.UiCommand

class DeactivateEffectCommand(
    val activeGroups: MutableList<FlaskEffectSelectionPage.EffectGroup>,
    val learnedGroups: MutableList<FlaskEffectSelectionPage.EffectGroup>,
    val group: FlaskEffectSelectionPage.EffectGroup,
    var newIndex: Int = learnedGroups.size
) : UiCommand {

    private var previousIndex: Int = -1

    override fun execute(commandBuilder: UICommandBuilder, eventBuilder: UIEventBuilder): Boolean {
        if (group.activeEffect == null) return false

        group.activeEffect = null
        previousIndex = activeGroups.indexOf(group)

        val activeSelector = FlaskEffectSelectionPage.getActiveEffectSelector(previousIndex)
        commandBuilder.remove(activeSelector)
        activeGroups.remove(group)

        learnedGroups.add(newIndex, group)

        for (i in newIndex until learnedGroups.size) {
            if (i >= learnedGroups.size - 1) {
                commandBuilder.append("#LearnedEffects #Content", "Pages/FlaskEffectLearnedItem.ui")
            }

            FlaskEffectSelectionPage.applyLearnedEffectElement(
                commandBuilder,
                eventBuilder,
                learnedGroups[i],
                i
            )
        }


        return true
    }

    override fun undo(): UiCommand {
        return ActivateEffectCommand(activeGroups, learnedGroups, group, previousIndex)
    }
}