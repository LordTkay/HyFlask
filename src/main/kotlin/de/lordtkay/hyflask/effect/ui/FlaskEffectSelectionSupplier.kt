package de.lordtkay.hyflask.effect.ui

import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.ComponentAccessor
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction.CustomPageSupplier
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import de.lordtkay.hyflask.effect.component.FlaskEffectComponent

class FlaskEffectSelectionSupplier : CustomPageSupplier {

    companion object {
        const val ID = "HyFlask_FlaskEffectSelection"

        val CODEC: BuilderCodec<FlaskEffectSelectionSupplier?>

        init {
            val builder = BuilderCodec.builder(
                FlaskEffectSelectionSupplier::class.java,
                ::FlaskEffectSelectionSupplier
            )

            CODEC = builder.build()
        }
    }

    override fun tryCreate(
        ref: Ref<EntityStore?>,
        componentAccessor: ComponentAccessor<EntityStore?>,
        playerRef: PlayerRef,
        interactionContext: InteractionContext
    ): CustomUIPage {
        val flaskEffectComponent = componentAccessor.ensureAndGetComponent(ref, FlaskEffectComponent.componentType)
        return FlaskEffectSelectionPage(playerRef, flaskEffectComponent)
    }
}
