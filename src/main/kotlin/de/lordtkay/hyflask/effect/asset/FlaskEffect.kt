package de.lordtkay.hyflask.effect.asset

import com.hypixel.hytale.assetstore.AssetExtraInfo
import com.hypixel.hytale.assetstore.AssetKeyValidator
import com.hypixel.hytale.assetstore.AssetRegistry
import com.hypixel.hytale.assetstore.AssetStore
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec
import com.hypixel.hytale.assetstore.map.IndexedAssetMap
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorSectionStart
import com.hypixel.hytale.codec.schema.metadata.ui.UIRebuildCaches
import com.hypixel.hytale.codec.validation.ValidatorCache
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTranslationProperties
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction
import com.hypixel.hytale.server.core.util.MessageUtil
import de.lordtkay.hyflask.effect.protocol.FlaskEffectInteractionType

/**
 * Asset type responsible for representing flask effects.
 */
class FlaskEffect : JsonAssetWithMap<String, IndexedAssetMap<String, FlaskEffect>> {

    companion object {
        const val ASSET_PATH = "HyFlask/FlaskEffects"
        val CODEC: AssetBuilderCodec<String, FlaskEffect>
        val VALIDATOR_CACHE = ValidatorCache(AssetKeyValidator(FlaskEffect::assetStore))
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
                .appendInherited(
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

            builder
                .appendInherited(
                    KeyedCodec("GroupDetails", FlaskEffectGroupDetails.CODEC),
                    { asset, value -> asset.groupDetails = value },
                    { asset -> asset.groupDetails },
                    { asset, parent -> asset.groupDetails = parent.groupDetails }
                )
                .documentation("A Group is used when the effect has multiple levels. It will group all effects together that use the same ID.")
                .add()

            builder.afterDecode(FlaskEffect::processConfig)

            CODEC = builder.build()
        }

        val assetStore: AssetStore<String, FlaskEffect, IndexedAssetMap<String, FlaskEffect>>
            get() {
                if (ASSET_STORE == null) {
                    ASSET_STORE = AssetRegistry.getAssetStore(FlaskEffect::class.java)
                }
                return ASSET_STORE!!
            }

        val assetMap: IndexedAssetMap<String, FlaskEffect>
            get() = assetStore.assetMap
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
    var groupDetails: FlaskEffectGroupDetails? = null
        private set

    constructor()

    constructor(id: String) {
        this.id = id
    }

    override fun getId(): String {
        return id
    }

    /**
     * Provides a display name for the effect, which is either the translated name or the `id` if no translation
     * is defined.
     */
    val displayName: String
        get() {
            translationProperties?.name?.let {
                val message = Message.translation(it)
                return MessageUtil.toAnsiString(message).toString()
            }

            return id
        }

    /**
     * Provides a display name for the effect, including the effect's unique identifier in parentheses. If the asset
     * does not have a translation, it returns the `id` directly.
     */
    val displayNameWithId: String
        get() {
            val translatedName = displayName
            if (translatedName == id) return id
            return "$translatedName ($id)"
        }

    /**
     * Provides a description for the effect, which is either the translated description or an empty string if no
     * translation is defined.
     */
    val description: String?
        get() {
            translationProperties?.description?.let {
                val message = Message.translation(it)
                return MessageUtil.toAnsiString(message).toString()
            }

            return null
        }


    private fun processConfig() {
        val itemQualityAssetMap = ItemQuality.getAssetMap()
        if (this.qualityId != null) {
            this.qualityIndex = itemQualityAssetMap.getIndexOrDefault(this.qualityId, 0)
        }
    }
}
