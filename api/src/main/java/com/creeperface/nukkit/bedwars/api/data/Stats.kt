package com.creeperface.nukkit.bedwars.api.data

import com.creeperface.nukkit.bedwars.api.utils.get
import com.creeperface.nukkit.bedwars.api.utils.set

class Stats(private val statsOriginal: Array<Int>) {

    private val stats = Array(Stat.values().size) { 0 }

    fun getDelta(stat: Stat): Int {
        return stats[stat]
    }

    fun add(stat: Stat, value: Int = 1) {
        stats[stat] = stats[stat] + value
    }

    operator fun get(stat: Stat): Int {
        return stats[stat] + statsOriginal[stat]
    }

    companion object {

        fun initial() = Stats(Array(Stat.values().size) { 0 })
    }
}
