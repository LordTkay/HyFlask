package de.lordtkay.hyflask.effect.ui.event

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import de.lordtkay.hyflask.effect.asset.FlaskEffect
import de.lordtkay.hyflask.effect.ui.FlaskEffectSelectionPage
import de.lordtkay.hyflask.utility.ui.command.UiCommand

class DeactivateEffectUiCommand(
    val activeGroups: MutableMap<String, FlaskEffectSelectionPage.EffectGroup>,
    val learnedGroups: MutableMap<String, FlaskEffectSelectionPage.EffectGroup>,
    val group: FlaskEffectSelectionPage.EffectGroup,
) : UiCommand {

    private var previousActiveEffect: FlaskEffect? = null

    override fun execute(commandBuilder: UICommandBuilder, eventBuilder: UIEventBuilder): Boolean {
        if (group.activeEffect == null) return false

        previousActiveEffect = group.activeEffect
        group.activeEffect = null

        val previousIndex = activeGroups.values.indexOf(group)
        val activeSelector = FlaskEffectSelectionPage.getActiveEffectSelector(previousIndex)
        commandBuilder.remove(activeSelector)
        activeGroups.remove(group.name)

        learnedGroups[group.name] = group
        val newIndex = learnedGroups.values.indexOf(group)

        for (i in newIndex until learnedGroups.size) {
            if (i >= learnedGroups.size - 1) {
                commandBuilder.append("#LearnedEffects #Content", "Pages/FlaskEffectLearnedItem.ui")
            }

            FlaskEffectSelectionPage.applyLearnedEffectElement(
                commandBuilder,
                eventBuilder,
                learnedGroups.values.elementAt(i),
                i
            )
        }


        return true
    }

    override fun undo(): UiCommand {
        return ActivateEffectUiCommand(activeGroups, learnedGroups, group, previousActiveEffect)
    }
}