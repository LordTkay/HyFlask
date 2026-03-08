package de.lordtkay.hyflask.command

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection
import de.lordtkay.hyflask.uses.command.UsesCommandCollection
import de.lordtkay.hyflask.capacity.command.CapacityCommandCollection
import de.lordtkay.hyflask.effect.command.EffectsCommandCollection

class HyFlaskCommandCollection : AbstractCommandCollection("hyflask", "server.hyflask.commands") {

    init {
        addSubCommand(EffectsCommandCollection())
        addSubCommand(CapacityCommandCollection())
        addSubCommand(UsesCommandCollection())
    }
}