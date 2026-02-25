package de.lordtkay.hyflask.effect.ui.event

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import de.lordtkay.hyflask.effect.asset.FlaskEffect
import de.lordtkay.hyflask.effect.ui.FlaskEffectSelectionPage
import de.lordtkay.hyflask.utility.ui.command.UiCommand

class ActivateEffectCommand(
    val activeGroups: MutableList<FlaskEffectSelectionPage.EffectGroup>,
    val learnedGroups: MutableList<FlaskEffectSelectionPage.EffectGroup>,
    val group: FlaskEffectSelectionPage.EffectGroup,
    var newIndex: Int = activeGroups.size,
    var activateEffect: FlaskEffect? = null
) : UiCommand {

    private var previousIndex: Int = -1

    override fun execute(commandBuilder: UICommandBuilder, eventBuilder: UIEventBuilder): Boolean {
        if (group.activeEffect != null) return false

        previousIndex = learnedGroups.indexOf(group)

        val learnedSelector = FlaskEffectSelectionPage.getLearnedEffectSelector(previousIndex)
        commandBuilder.remove(learnedSelector)
        learnedGroups.remove(group)

        activeGroups.add(newIndex, group)
        group.activeEffect = activateEffect ?: group.learnedEffects.firstEntry().value

        for (i in newIndex until activeGroups.size) {
            if (i >= activeGroups.size - 1) {
                commandBuilder.append("#ActiveEffects #EffectList", "Pages/FlaskEffectActiveItem.ui")

            }
            FlaskEffectSelectionPage.applyActiveEffectElement(
                commandBuilder,
                eventBuilder,
                activeGroups[i],
                i
            )
        }

        return true
    }

    override fun undo(): UiCommand {
        return DeactivateEffectCommand(activeGroups, learnedGroups, group, previousIndex)
    }
}