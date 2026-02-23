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
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor
import com.hypixel.hytale.codec.schema.metadata.ui.UIRebuildCaches
import com.hypixel.hytale.codec.validation.ValidatorCache
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTranslationProperties
import com.hypixel.hytale.server.core.util.MessageUtil

/**
 * Asset type responsible for representing flask effect group.
 */
class FlaskEffectGroup : JsonAssetWithMap<String, IndexedAssetMap<String, FlaskEffectGroup>> {

    companion object {
        const val ASSET_PATH = "HyFlask/FlaskEffectsGroup"
        val CODEC: AssetBuilderCodec<String, FlaskEffectGroup>
        val VALIDATOR_CACHE: ValidatorCache<String>
        private var ASSET_STORE: AssetStore<String, FlaskEffectGroup, IndexedAssetMap<String, FlaskEffectGroup>>? = null

        init {
            val builder = AssetBuilderCodec.builder(
                FlaskEffectGroup::class.java,
                ::FlaskEffectGroup,
                Codec.STRING,
                { asset, id -> asset.id = id },
                { asset -> asset.id },
                { asset, data -> asset.data = data },
                { asset -> asset.data }
            )

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
                    KeyedCodec("Icon", Codec.STRING),
                    { asset, value -> asset.icon = value },
                    { asset -> asset.icon },
                    { asset, parent -> asset.icon = parent.icon }
                )
                .addValidator(CommonAssetValidator.ICON_ITEM)
                .metadata(UIEditor(UIEditor.Icon("Icons/Items/FlaskEffectsGroup/{assetId}.png", 64, 64)))
                .metadata(UIRebuildCaches(UIRebuildCaches.ClientCache.ITEM_ICONS))
                .add()

            CODEC = builder.build()
            VALIDATOR_CACHE = ValidatorCache(AssetKeyValidator(FlaskEffectGroup::assetStore))
        }

        val assetStore: AssetStore<String, FlaskEffectGroup, IndexedAssetMap<String, FlaskEffectGroup>>
            get() {
                if (ASSET_STORE == null) {
                    ASSET_STORE = AssetRegistry.getAssetStore(FlaskEffectGroup::class.java)
                }
                return ASSET_STORE!!
            }

        val assetMap: IndexedAssetMap<String, FlaskEffectGroup>
            get() = assetStore.assetMap
    }

    private var id: String = ""

    var data: AssetExtraInfo.Data? = null
        private set
    var translationProperties: ItemTranslationProperties? = null
        private set
    var icon: String? = null
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
}
