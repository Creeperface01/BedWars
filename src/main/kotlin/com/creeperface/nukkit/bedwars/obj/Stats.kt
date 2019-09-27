package com.creeperface.nukkit.bedwars.obj

import cn.nukkit.Player
import com.creeperface.nukkit.bedwars.mysql.Stat
import java.util.*

class Stats(val player: Player) {

    private val stats = EnumMap<Stat, Int>(Stat::class.java)
    private val statsOriginal = EnumMap<Stat, Int>(Stat::class.java)

    fun init(data: Map<String, Any>) {
        statsOriginal[Stat.KILLS] = data["kills"] as Int
        statsOriginal[Stat.DEATHS] = data["deaths"] as Int
        statsOriginal[Stat.WINS] = data["wins"] as Int
        statsOriginal[Stat.LOSSES] = data["losses"] as Int
        statsOriginal[Stat.BEDS] = data["beds"] as Int

        stats[Stat.KILLS] = 0
        stats[Stat.DEATHS] = 0
        stats[Stat.WINS] = 0
        stats[Stat.LOSSES] = 0
        stats[Stat.BEDS] = 0
    }

    operator fun get(stat: Stat): Int {
        return stats[stat]!! + statsOriginal[stat]!!
    }

    fun getDelta(stat: Stat): Int {
        return stats[stat]!!
    }

    @JvmOverloads
    fun add(stat: Stat, value: Int = 1) {
        stats[stat] = stats[stat]!! + value
    }
}
