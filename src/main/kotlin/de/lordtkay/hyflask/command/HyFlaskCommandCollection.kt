package de.lordtkay.hyflask.command

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection
import de.lordtkay.hyflask.capacity.command.CapacityCommandCollection
import de.lordtkay.hyflask.effect.command.EffectsCommandCollection
import de.lordtkay.hyflask.enumeration.TRANSLATION_ROOT
import de.lordtkay.hyflask.uses.command.UsesCommandCollection

class HyFlaskCommandCollection(
    translationKey: String = "$TRANSLATION_ROOT.commands"
) : AbstractCommandCollection("hyflask", translationKey) {

    init {
        addSubCommand(EffectsCommandCollection(translationKey))
        addSubCommand(CapacityCommandCollection(translationKey))
        addSubCommand(UsesCommandCollection(translationKey))
    }
}