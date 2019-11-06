package com.creeperface.nukkit.bedwars.task

import cn.nukkit.Server
import cn.nukkit.scheduler.AsyncTask
import cn.nukkit.utils.LevelException
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.arena.manager.WorldManager

class WorldCopyTask constructor(private val plugin: BedWars, private val map: String, private val id: String, private val force: Boolean = false) : AsyncTask() {

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
            e.printStackTrace()
            server.logger.error("Error while loading level: " + this.map)
            return
        }

        val arena = plugin.getArena(id)

        if (arena != null) {
            arena.isLevelLoaded = true

            if (force) {
                arena.startGame()
            }
        }
    }

}