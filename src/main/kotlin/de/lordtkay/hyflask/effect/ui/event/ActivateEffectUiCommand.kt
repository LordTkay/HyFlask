package de.lordtkay.hyflask.effect.ui.event

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import de.lordtkay.hyflask.effect.asset.FlaskEffect
import de.lordtkay.hyflask.effect.ui.FlaskEffectSelectionPage
import de.lordtkay.hyflask.utility.ui.command.UiCommand

class ActivateEffectUiCommand(
    val activeGroups: MutableMap<String, FlaskEffectSelectionPage.EffectGroup>,
    val learnedGroups: MutableMap<String, FlaskEffectSelectionPage.EffectGroup>,
    val group: FlaskEffectSelectionPage.EffectGroup,
    var activateEffect: FlaskEffect? = null
) : UiCommand {


    override fun execute(commandBuilder: UICommandBuilder, eventBuilder: UIEventBuilder): Boolean {
        if (group.activeEffect != null) return false

        val previousIndex = learnedGroups.values.indexOf(group)
        val learnedSelector = FlaskEffectSelectionPage.getLearnedEffectSelector(previousIndex)
        commandBuilder.remove(learnedSelector)
        learnedGroups.remove(group.name)

        activeGroups[group.name] = group
        val newIndex = activeGroups.values.indexOf(group)

        group.activeEffect = activateEffect ?: group.learnedEffects.firstEntry().value

        for (i in newIndex until activeGroups.size) {
            if (i >= activeGroups.size - 1) {
                commandBuilder.append("#ActiveEffects #EffectList", "Pages/FlaskEffectActiveItem.ui")

            }
            FlaskEffectSelectionPage.applyActiveEffectElement(
                commandBuilder,
                eventBuilder,
                activeGroups.values.elementAt(i),
                i
            )
        }

        return true
    }

    override fun undo(): UiCommand {
        return DeactivateEffectUiCommand(activeGroups, learnedGroups, group)
    }
}