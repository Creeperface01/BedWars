package com.creeperface.nukkit.bedwars.dataprovider

import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.api.data.Stats
import com.creeperface.nukkit.bedwars.api.data.provider.DataProvider
import com.creeperface.nukkit.bedwars.api.utils.set
import com.creeperface.nukkit.bedwars.utils.toMap
import ru.nukkit.dblib.DbLib
import java.sql.Connection

class MySQLDataProvider : DataProvider {

    private val connection: Connection
        get() = DbLib.getDefaultConnection()

    override fun init() {
        connection.use { con ->
            con.prepareStatement("CREATE TABLE IF NOT EXISTS bedwars (" +
                    "id INT PRIMARY KEY auto_increment," +
                    "identifier VARCHAR(128) NOT NULL UNIQUE," +
                    "name VARCHAR(64)," +
                    "kills INT DEFAULT 0," +
                    "deaths INT DEFAULT 0," +
                    "wins INT DEFAULT 0," +
                    "losses INT DEFAULT 0," +
                    "beds INT DEFAULT 0," +
                    "place INT DEFAULT 0," +
                    "break INT DEFAULT 0," +
                    "games INT DEFAULT 0" +
                    ")"
            )
        }
    }

    override suspend fun register(name: String, identifier: String) {
        connection.use { con ->
            con.prepareStatement("INSERT INTO bedwars (identifier, name) VALUES(?, ?)").use { statement ->
                statement.setString(1, identifier)
                statement.setString(2, name)

                statement.executeUpdate()
            }
        }
    }

    override suspend fun unregister(identifier: String) {
        connection.use { con ->
            con.prepareStatement("REMOVE FROM bedwars WHERE identifier = ?").use { statement ->
                statement.setString(1, identifier)

                statement.executeUpdate()
            }
        }
    }

    override suspend fun getData(identifier: String): Stats {
        connection.use { con ->
            con.prepareStatement("SELECT (kills, deaths, wins, losses, beds, placed, broken, games) FROM bedwars WHERE identifier = ?").use { statement ->
                statement.setString(1, identifier)

                val result = statement.executeQuery()
                val stats = Array(Stat.values().size) { 0 }

                result.toMap().forEach { (key, value) ->
                    stats[Stat.valueOf(key)] = value as Int
                }

                return Stats(stats)
            }
        }
    }

    override suspend fun getDataByName(name: String): Stats {
        connection.use { con ->
            con.prepareStatement("SELECT (kills, deaths, wins, losses, beds, placed, broken, games) FROM bedwars WHERE name = ?").use { statement ->
                statement.setString(1, name)

                val result = statement.executeQuery()
                val stats = Array(Stat.values().size) { 0 }

                result.toMap().forEach { (key, value) ->
                    stats[Stat.valueOf(key)] = value as Int
                }

                return Stats(stats)
            }
        }
    }

    override suspend fun saveData(identifier: String, data: Stats) {
        connection.use { con ->
            val stats = Stat.values().joinToString(",") { it.name.toLowerCase() + "=?" }

            con.prepareStatement("UPDATE bedwars SET ($stats) WHERE identifier = ?").use { statement ->
                statement.setString(1, identifier)

                Stat.values().forEachIndexed { index, stat ->
                    statement.setInt(index + 2, data[stat])
                }

                statement.executeUpdate()
            }
        }
    }
}