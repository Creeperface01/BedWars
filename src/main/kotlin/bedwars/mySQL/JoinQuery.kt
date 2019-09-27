package bedwars.mySQL

import bedwars.BedWars
import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.math.Vector3
import cn.nukkit.scheduler.AsyncTask
import java.sql.SQLException
import java.util.*

class JoinQuery(plugin: BedWars, private val playerInstance: Player) : AsyncTask() {

    internal var registered = false

    internal lateinit var data: Map<String, Any>

    init {
        plugin.server.scheduler.scheduleAsyncTask(plugin, this)
    }

    override fun onRun() {
        MySQLManager.connection.use {
            it.prepareStatement("SELECT * FROM bedwars WHERE name = ?").use { statement ->
                statement.setString(1, playerInstance.name)
                statement.executeQuery().use { result ->
                    if (result.next()) {
                        this.data = result.toMap()
                    } else {
                        this.data = registerPlayer(playerInstance.name)
                    }
                }
            }
        }
    }

    fun registerPlayer(player: String): HashMap<String, Any> {
        val name = player.toLowerCase().trim { it == ' ' }

        val data = HashMap<String, Any>()
        data["name"] = name
        data["kills"] = 0
        data["deaths"] = 0
        data["wins"] = 0
        data["losses"] = 0
        data["beds"] = 0

        try {
            MySQLManager.connection.use { con ->
                con.prepareStatement("INSERT INTO bedwars ( name, kills, deaths, wins, losses, beds) VALUES (?, 0, 0, 0, 0, 0)").use {
                    it.setString(1, name)
                    it.executeUpdate()
                }
            }

            registered = true
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return data
    }

    override fun onCompletion(server: Server?) {
        if (!playerInstance.isOnline) {
            return
        }

        this.playerInstance.teleport(Vector3(128.5, 50.0, 128.5))

        val globalData = BedWars.instance.players[this.playerInstance.id]
        globalData?.stats?.init(data)
    }
}