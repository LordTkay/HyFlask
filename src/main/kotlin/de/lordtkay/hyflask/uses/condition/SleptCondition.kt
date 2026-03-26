package de.lordtkay.hyflask.uses.condition

import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSleep
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSomnolence
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor
import com.hypixel.hytale.component.ComponentAccessor
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.modules.entity.condition.Condition
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.NotificationUtil
import java.time.Instant

class SleptCondition : Condition() {
    companion object {
        private val logger = HytaleLogger.forEnclosingClass()

        val CODEC: BuilderCodec<SleptCondition>

        init {
            var builder = BuilderCodec.builder(
                SleptCondition::class.java,
                ::SleptCondition,
                BASE_CODEC
            ).documentation("Checks if the player has the 'MorningWakeUp' state of the 'PlayerSomnolence' component.")
            builder.append(
                KeyedCodec("Message", Codec.STRING),
                { component, value -> component.message = value },
                { component -> component.message }
            )
                .documentation("The message displayed if the condition is true.")
                .metadata(
                    UIEditor(
                        UIEditor.LocalizationKeyField(
                            "server.hyflask.notification.{assetId}.success",
                            false
                        )
                    )
                )
                .add()
            CODEC = builder.build()
        }
    }

    var message: String? = null
        private set

    override fun eval0(
        componentAccessor: ComponentAccessor<EntityStore?>,
        ref: Ref<EntityStore?>,
        currentTime: Instant
    ): Boolean {

        val somnolence = componentAccessor.getComponent(ref, PlayerSomnolence.getComponentType()) ?: return false

        if (somnolence.sleepState !is PlayerSleep.MorningWakeUp) {
            return false
        }

        if (message != null) {
            val msg = Message.translation(message!!)
            val playerRef = componentAccessor.getComponent(ref, PlayerRef.getComponentType())
            if (playerRef != null) {
                NotificationUtil.sendNotification(playerRef.packetHandler, msg)
            }
        }
        return true
    }
}