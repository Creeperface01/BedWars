package com.creeperface.nukkit.bedwars.task

import cn.nukkit.Server
import cn.nukkit.scheduler.AsyncTask
import cn.nukkit.utils.LevelException
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.utils.handle
import com.creeperface.nukkit.bedwars.arena.ArenaState
import com.creeperface.nukkit.bedwars.arena.manager.WorldManager
import com.creeperface.nukkit.bedwars.utils.logError

class WorldCopyTask constructor(
    private val plugin: BedWars,
    private val map: String,
    private val id: String,
    private val force: Boolean = false
) : AsyncTask() {

    init {
        plugin.server.scheduler.scheduleAsyncTask(plugin, this)
    }

    override fun onRun() {
        WorldManager.resetWorld(this.map, this.id)
    }

    override fun onCompletion(server: Server) {
        try {
            server.loadLevel(this.map + "_" + this.id)
        } catch (e: LevelException) {
            logError("Error while loading level: " + this.map, e)
            return
        }

        val arena = plugin.getArena(id)

        if (arena != null) {
            if (force) {
                arena.handle(ArenaState.TEAM_SELECT) {
                    forceStart()
                }
            }
        }
    }

}