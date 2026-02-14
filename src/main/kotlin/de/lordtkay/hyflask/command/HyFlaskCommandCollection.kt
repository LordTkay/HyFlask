package de.lordtkay.hyflask.command

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection
import de.lordtkay.hyflask.uses.command.UsesCommandCollection

class HyFlaskCommandCollection : AbstractCommandCollection("hyflask", "server.hyflask.commands") {

    init {
        addSubCommand(UsesCommandCollection())
    }
}