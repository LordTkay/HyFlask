package de.lordtkay.hyflask.effect.command

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection

class EffectsCommandCollection : AbstractCommandCollection("effects", "server.hyflask.commands.effects") {

    init {
        addSubCommand(LearnEffectCommand())
        addSubCommand(ForgetEffectCommand())
        addSubCommand(ActivateEffectCommand())
    }

}