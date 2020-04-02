package com.creeperface.nukkit.bedwars.obj

import cn.nukkit.Player
import com.creeperface.nukkit.bedwars.api.arena.PlayerData
import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.arena.Team

class BedWarsData(override val arena: Arena,
                  override val player: Player,
                  val globalData: GlobalData) : PlayerData {

    override lateinit var team: Team

    var lastHit: Long = 0

    var killer: String? = null

    var killerColor: String? = null

    var points = 0

    override fun hasTeam() = ::team.isInitialized

    fun canRespawn(): Boolean {
        return this.team.hasBed()
    }

    fun wasKilled(): Boolean {
        return System.currentTimeMillis() - lastHit <= 10000
    }

    fun addStat(stat: Stat) {
        this.globalData.stats.add(stat)

        arena.plugin.economyRewards[stat]?.let { rewards ->
            rewards.forEach {
                arena.plugin.economyProvider.addMoney(player, it.amount, it.currency)
            }
        }
    }
}
