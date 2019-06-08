package bedWars.task

import bedWars.BedWars
import bedWars.arena.WorldManager
import cn.nukkit.Server
import cn.nukkit.scheduler.AsyncTask
import cn.nukkit.utils.LevelException

class WorldCopyTask @JvmOverloads constructor(private val plugin: BedWars, private val map: String, private val id: String, private val force: Boolean = false) : AsyncTask() {

    init {

        plugin.server.scheduler.scheduleAsyncTask(plugin, this)
    }

    override fun onRun() {
        WorldManager.resetWorld(this.map, this.id)
    }

    override fun onCompletion(server: Server?) {
        try {
            server!!.loadLevel(this.map + "_" + this.id)
        } catch (e: LevelException) {
            e.printStackTrace()
            server!!.logger.error("Error while loading level: " + this.map)
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