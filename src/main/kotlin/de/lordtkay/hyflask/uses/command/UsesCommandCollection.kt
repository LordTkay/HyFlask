package de.lordtkay.hyflask.uses.command

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection

class UsesCommandCollection : AbstractCommandCollection("uses", "server.hyflask.commands.uses") {

    init {
        addSubCommand(GetUsesCommand())
        addSubCommand(AddUsesCommand())
        addSubCommand(SetToMaxUsesCommand())
        addSubCommand(SetUsesCommand())
        addSubCommand(AddMaxUsesCommand())
        addSubCommand(SetMaxUsesCommand())
        addSubCommand(ResetMaxUsesCommand())
    }
}