package de.lordtkay.hyflask.effect.command

import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.util.MessageUtil
import de.lordtkay.hyflask.effect.asset.FlaskEffect

fun fetchEffect(
    logger: HytaleLogger,
    playerRef: PlayerRef,
    effectId: String
): String? {
    val asset = FlaskEffect.assetMap.getAsset(effectId)
    if (asset == null) {
        val message = Message.translation("server.hyflask.commands.effects.invalidEffectId")
            .param("id", effectId)
        playerRef.sendMessage(message)

        logger.atWarning().log("Could not find flask effect asset with ID '$effectId'")
        return null
    }

    val assetTranslationName = asset.translationProperties?.name ?: return asset.id

    val assetTranslatedName = MessageUtil.toAnsiString(Message.translation(assetTranslationName))
    return "$assetTranslatedName (${asset.id})"
}