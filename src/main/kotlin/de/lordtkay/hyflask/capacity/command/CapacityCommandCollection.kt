package de.lordtkay.hyflask.capacity.command

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection

class CapacityCommandCollection : AbstractCommandCollection("capacity", "server.hyflask.commands.capacity") {
    init {
        addSubCommand(AddMaxCapacityCommand())
        addSubCommand(ResetMaxCapacityCommand())
        addSubCommand(SetMaxCapacityCommand())
    }
}