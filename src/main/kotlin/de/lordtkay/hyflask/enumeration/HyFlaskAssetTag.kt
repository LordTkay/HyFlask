package de.lordtkay.hyflask.enumeration

import com.hypixel.hytale.assetstore.AssetExtraInfo
import com.hypixel.hytale.assetstore.AssetRegistry

enum class HyFlaskAssetTag(vararg val ids: String) {
    HYFLASK_EFFECT("HyFlask_Effect"),
    WATER_BREATHING("HyFlask_Effect", "WaterBreathing"),
    RECALL("HyFlask_Effect", "Recall"),
    JUMP_HEIGHT("HyFlask_Effect", "JumpHeight");

    /**
     * Checks whether the given data contains a tag sequence that matches the tag identifiers defined in this asset tag.
     * The method validates the presence of each tag in the sequence and ensures proper tag relationships.
     *
     * @param data The asset data containing tag information to be verified.
     * @return True if the tag sequence exists and satisfies the required structure, otherwise false.
     */
    fun exists(data: AssetExtraInfo.Data): Boolean {
        val tagIndices = ids.map { AssetRegistry.getTagIndex(it) }

        tagIndices.forEachIndexed { index, tagIndex ->
            val tag = data.getTag(tagIndex) ?: return false
            if (index >= tagIndices.size - 1) return@forEachIndexed
            if (!tag.contains(tagIndices[index + 1])) return false
        }

        return true
    }
}