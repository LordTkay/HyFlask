package de.lordtkay.hyflask.effect.asset

import com.hypixel.hytale.assetstore.AssetExtraInfo
import com.hypixel.hytale.assetstore.AssetRegistry
import com.hypixel.hytale.assetstore.AssetStore
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec
import com.hypixel.hytale.assetstore.map.IndexedAssetMap
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec
import com.hypixel.hytale.codec.schema.metadata.ui.*
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTranslationProperties
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction
import de.lordtkay.hyflask.effect.protocol.FlaskEffectInteractionType

/**
 * Asset type responsible for representing flask effects.
 */
class FlaskEffect : JsonAssetWithMap<String, IndexedAssetMap<String, FlaskEffect>> {

    companion object {
        const val ASSET_PATH = "Hyflask/Effects"
        val CODEC: AssetBuilderCodec<String, FlaskEffect>
        private var ASSET_STORE: AssetStore<String, FlaskEffect, IndexedAssetMap<String, FlaskEffect>>? = null

        init {
            val builder = AssetBuilderCodec.builder(
                FlaskEffect::class.java,
                ::FlaskEffect,
                Codec.STRING,
                { asset, id -> asset.id = id },
                { asset -> asset.id },
                { asset, data -> asset.data = data },
                { asset -> asset.data }
            )

            builder
                .appendInherited(/**/
                    KeyedCodec("Icon", Codec.STRING),
                    { asset, value -> asset.icon = value },
                    { asset -> asset.icon },
                    { asset, parent -> asset.icon = parent.icon }
                )
                .addValidator(CommonAssetValidator.ICON_ITEM)
                .metadata(UIEditor(UIEditor.Icon("Icons/Items/FlaskEffects/{assetId}.png", 64, 64)))
                .metadata(UIRebuildCaches(UIRebuildCaches.ClientCache.ITEM_ICONS))
                .add()

            builder
                .appendInherited(
                    KeyedCodec("TranslationProperties", ItemTranslationProperties.CODEC),
                    { asset, value -> asset.translationProperties = value },
                    { asset -> asset.translationProperties },
                    { asset, parent -> asset.translationProperties = parent.translationProperties }
                )
                .documentation("The translation properties for this item asset.")
                .add()

            builder
                .appendInherited(
                    KeyedCodec("Quality", Codec.STRING),
                    { asset, value -> asset.qualityId = value },
                    { asset -> asset.qualityId },
                    { asset, parent -> asset.qualityId = parent.qualityId }
                )
                .addValidator(ItemQuality.VALIDATOR_CACHE.getValidator())
                .add()

            builder
                .appendInherited(
                    KeyedCodec(
                        "Interactions",
                        EnumMapCodec(FlaskEffectInteractionType::class.java, RootInteraction.CHILD_ASSET_CODEC)
                    ),
                    { asset, value -> asset.interactions = value },
                    { asset -> asset.interactions },
                    { asset, parent -> asset.interactions = parent.interactions }
                )
                .addValidator(RootInteraction.VALIDATOR_CACHE.mapValueValidator)
                .metadata(UIEditorSectionStart("Interactions"))
                .add()

            builder
                .appendInherited(
                    KeyedCodec(
                        "InteractionVars",
                        MapCodec(RootInteraction.CHILD_ASSET_CODEC, ::HashMap)
                    ),
                    { asset, value -> asset.interactionVars = value },
                    { asset -> asset.interactionVars },
                    { asset, parent -> asset.interactionVars = parent.interactionVars }
                )
                .addValidator(RootInteraction.VALIDATOR_CACHE.mapValueValidator)
                .add()

            builder.afterDecode(FlaskEffect::processConfig)

            CODEC = builder.build()
        }

        fun getAssetStore(): AssetStore<String, FlaskEffect, IndexedAssetMap<String, FlaskEffect>> {
            if (ASSET_STORE == null) {
                ASSET_STORE = AssetRegistry.getAssetStore(FlaskEffect::class.java)
            }
            return ASSET_STORE!!
        }

        fun getAssetMap(): IndexedAssetMap<String, FlaskEffect> {
            return getAssetStore().assetMap
        }
    }

    private var id: String = ""

    var data: AssetExtraInfo.Data? = null
        private set
    var translationProperties: ItemTranslationProperties? = null
        private set
    var icon: String? = null
        private set
    var qualityId: String? = null
        private set
    var qualityIndex: Int = 0
        private set
    var interactions: MutableMap<FlaskEffectInteractionType, String> = mutableMapOf()
        private set
    var interactionVars: MutableMap<String, String> = mutableMapOf()
        private set

    constructor()

    constructor(id: String) {
        this.id = id
    }

    override fun getId(): String {
        return id
    }

    private fun processConfig() {
        val itemQualityAssetMap = ItemQuality.getAssetMap()
        if (this.qualityId != null) {
            this.qualityIndex = itemQualityAssetMap.getIndexOrDefault(this.qualityId, 0)
        }
    }
}
