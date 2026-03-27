package de.lordtkay.hyflask.statistic.component

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

class FlaskStatisticComponent : Component<EntityStore?> {

    companion object {
        private var logger = HytaleLogger.forEnclosingClass()

        lateinit var componentType: ComponentType<EntityStore?, FlaskStatisticComponent>

        val CODEC: BuilderCodec<FlaskStatisticComponent>

        init {
            val builder = BuilderCodec.builder(FlaskStatisticComponent::class.java, ::FlaskStatisticComponent)

            builder
                .append(
                    KeyedCodec("NumberOfUses", Codec.INTEGER),
                    { component, value -> component.numberOfUses = value },
                    { component -> component.numberOfUses }
                )
                .documentation("The amount of times the player has used the flask.")
                .add()

            CODEC = builder.build()
        }
    }

    var numberOfUses: Int = 0
        private set

    fun add(type: Type, amount: Int): Int {
        return type.add(this, amount)
    }

    fun get(type: Type): Int {
        return type.get(this)
    }

    override fun clone(): FlaskStatisticComponent {
        val copy = FlaskStatisticComponent()
        copy.numberOfUses = numberOfUses
        return copy
    }

    enum class Type(val get: (FlaskStatisticComponent) -> Int, val add: (FlaskStatisticComponent, Int) -> Int) {
        USES(
            { component -> component.numberOfUses },
            { component, value ->
                component.numberOfUses += value
                component.numberOfUses
            }
        )
    }
}