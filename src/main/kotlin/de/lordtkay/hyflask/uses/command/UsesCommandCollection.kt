package de.lordtkay.hyflask.uses.command

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection

class UsesCommandCollection(
    parentTranslationKey: String,
    translationKey: String = "$parentTranslationKey.uses"
) : AbstractCommandCollection("uses", translationKey) {

    init {
        addSubCommand(GetUsesCommand(translationKey))
        addSubCommand(AddUsesCommand(translationKey))
        addSubCommand(SetToMaxUsesCommand(translationKey))
        addSubCommand(SetUsesCommand(translationKey))
        addSubCommand(AddMaxUsesCommand(translationKey))
        addSubCommand(SetMaxUsesCommand(translationKey))
        addSubCommand(ResetMaxUsesCommand(translationKey))
    }
}