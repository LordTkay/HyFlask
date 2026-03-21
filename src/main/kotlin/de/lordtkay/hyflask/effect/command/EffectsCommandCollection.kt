package de.lordtkay.hyflask.effect.command

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection

class EffectsCommandCollection(
    parentTranslationKey: String,
    translationKey: String = "$parentTranslationKey.effect"
) : AbstractCommandCollection("effect", translationKey) {

    init {
        addSubCommand(SelectEffectCommand(translationKey))
        addSubCommand(GetEffectCommand(translationKey))
        addSubCommand(LearnEffectCommand(translationKey))
        addSubCommand(ForgetEffectCommand(translationKey))
        addSubCommand(ActivateEffectCommand(translationKey))
        addSubCommand(DeactivateEffectCommand(translationKey))
    }

}