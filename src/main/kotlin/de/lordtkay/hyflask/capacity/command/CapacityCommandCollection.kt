package de.lordtkay.hyflask.capacity.command

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection

class CapacityCommandCollection(
    parentTranslationKey: String,
    translationKey: String = "$parentTranslationKey.capacity"
) : AbstractCommandCollection("capacity", translationKey) {
    init {
        addSubCommand(AddMaxCapacityCommand(translationKey))
        addSubCommand(ResetMaxCapacityCommand(translationKey))
        addSubCommand(SetMaxCapacityCommand(translationKey))
        addSubCommand(GetCapacityCommand(translationKey))
    }
}