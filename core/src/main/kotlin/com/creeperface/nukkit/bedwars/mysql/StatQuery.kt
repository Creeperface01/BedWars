package com.creeperface.nukkit.bedwars.mysql

import cn.nukkit.scheduler.AsyncTask
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.api.data.Stats

class StatQuery(plugin: BedWars, internal var stats: Stats) : AsyncTask() {

    init {

        if (!BedWars.instance.shuttingDown) {
            plugin.server.scheduler.scheduleAsyncTask(plugin, this)
        } else {
            onRun()
        }
    }

    override fun onRun() {
        val p = stats.player.name.toLowerCase()

        MySQLManager.connection.use {
            it.prepareStatement("UPDATE bedwars SET kills = kills+'" + stats.getDelta(Stat.KILLS) + "', deaths = deaths+'" + stats.getDelta(Stat.DEATHS) + "', wins = wins+'" + stats.getDelta(Stat.WINS) + "', losses = losses+'" + stats.getDelta(Stat.LOSSES) + "', beds = beds+'" + stats.getDelta(Stat.BEDS) + "' WHERE name = '" + p.trim { c -> c == ' ' }.toLowerCase() + "'").use { statement ->
                statement.executeUpdate()
            }
        }
    }
}