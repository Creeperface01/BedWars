package com.creeperface.nukkit.bedwars.obj

import cn.nukkit.Player
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.mysql.Stat

class BedWarsData(private val arena: Arena,
                  val player: Player,
                  val globalData: GlobalData) {

    var team: Team? = null

    var lastHit: Long = 0

    var killer: String? = null

    var killerColor: String? = null

    var points = 0


    fun canRespawn(): Boolean {
        return this.team!!.hasBed()
    }

    fun wasKilled(): Boolean {
        return System.currentTimeMillis() - lastHit <= 10000
    }

    fun add(stat: Stat) {
        this.globalData.stats.add(stat)

//        this.baseData.addMoney(stat.tokens) //TODO: money
//        this.baseData.addExp(stat.xp)
    }
}
