package de.lordtkay.hyflask.enumeration

import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType

enum class HyFlaskEntityStat(val id: String) {
    CAPACITY("HyFlask_Capacity");

    fun getIndex(): Int {
        return EntityStatType.getAssetMap().getIndex(id)
    }
}