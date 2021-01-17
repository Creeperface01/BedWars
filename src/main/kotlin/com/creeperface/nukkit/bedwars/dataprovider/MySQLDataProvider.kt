package com.creeperface.nukkit.bedwars.dataprovider

import com.creeperface.nukkit.bedwars.api.arena.configuration.ArenaConfiguration
import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.arena.configuration.MutableConfiguration
import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.api.data.Stats
import com.creeperface.nukkit.bedwars.api.data.provider.DataProvider
import com.creeperface.nukkit.bedwars.api.utils.set
import com.creeperface.nukkit.bedwars.utils.Configuration
import com.creeperface.nukkit.bedwars.utils.fromJson
import com.creeperface.nukkit.bedwars.utils.toMap
import ru.nukkit.dblib.DbLib
import java.sql.Connection
import java.sql.ResultSet

internal class MySQLDataProvider(private val configuration: Configuration) : DataProvider {

    private lateinit var connectionFactory: () -> Connection

    private val connection: Connection
        get() = connectionFactory()

    override fun init() {
        connectionFactory = if (configuration.useDbLib) {
            { DbLib.getDefaultConnection() }
        } else {
            val mysql = configuration.mysql

            {
                DbLib.getMySqlConnection(mysql.host, mysql.port, mysql.database, mysql.user, mysql.password)
            }
        }

        connection.use { con ->
            con.prepareStatement("CREATE TABLE IF NOT EXISTS $STATS_TABLE (" +
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
            ).use { statement ->
                statement.execute()
            }

            con.prepareStatement("CREATE TABLE IF NOT EXISTS $MAPS_TABLE (" +
                    "id INT PRIMARY KEY auto_increment," +
                    "name VARCHAR(64) NOT NULL UNIQUE," +
                    "data TEXT NOT NULL," +
                    "last_update TIMESTAMP," +
                    ")"
            ).use { statement ->
                statement.execute()
            }

            con.prepareStatement("CREATE TABLE IF NOT EXISTS $ARENAS_TABLE (" +
                    "id INT PRIMARY KEY auto_increment," +
                    "name VARCHAR(64) NOT NULL UNIQUE," +
                    "data TEXT NOT NULL," +
                    "last_update TIMESTAMP," +
                    ")"
            ).use { statement ->
                statement.execute()
            }
        }
    }

    override suspend fun register(name: String, identifier: String) {
        connection.use { con ->
            con.prepareStatement("INSERT INTO $STATS_TABLE (identifier, name) VALUES(?, ?)").use { statement ->
                statement.setString(1, identifier)
                statement.setString(2, name)

                statement.executeUpdate()
            }
        }
    }

    override suspend fun unregister(identifier: String) {
        connection.use { con ->
            con.prepareStatement("REMOVE FROM $STATS_TABLE WHERE identifier = ?").use { statement ->
                statement.setString(1, identifier)

                statement.executeUpdate()
            }
        }
    }

    override suspend fun getData(identifier: String): Stats? {
        connection.use { con ->
            con.prepareStatement("SELECT (kills, deaths, wins, losses, beds, placed, broken, games) FROM $STATS_TABLE WHERE identifier = ?").use { statement ->
                statement.setString(1, identifier)

                val result = statement.executeQuery()

                return parseResult(result)
            }
        }
    }

    override suspend fun getDataByName(name: String): Stats? {
        connection.use { con ->
            con.prepareStatement("SELECT (kills, deaths, wins, losses, beds, placed, broken, games) FROM $STATS_TABLE WHERE name = ?").use { statement ->
                statement.setString(1, name)

                val result = statement.executeQuery()

                return parseResult(result)
            }
        }
    }

    private fun parseResult(result: ResultSet): Stats? {
        if (!result.next()) {
            return null
        }

        val stats = Array(Stat.values().size) { 0 }

        result.toMap().forEach { (key, value) ->
            stats[Stat.valueOf(key)] = value as Int
        }

        return Stats(stats)
    }

    override suspend fun saveData(identifier: String, data: Stats) {
        connection.use { con ->
            val stats = Stat.values().joinToString(",") {
                val name = it.name.toLowerCase()
                "$name=$name+?"
            }

            con.prepareStatement("UPDATE $STATS_TABLE SET ($stats) WHERE identifier = ?").use { statement ->
                statement.setString(1, identifier)

                Stat.values().forEachIndexed { index, stat ->
                    statement.setInt(index + 2, data.getDelta(stat))
                }

                statement.executeUpdate()
            }
        }
    }

    private inline fun <reified T : MutableConfiguration> sync(data: MutableMap<String, T>, table: String) {
        connection.use { con ->
            con.prepareStatement("SELECT * FROM $table").use { statement ->
                val result = statement.executeQuery()

                while (result.next()) {
                    val name = result.getString("name")
                    val local = data[name]

                    val modifyTime = result.getTimestamp("last_update").toInstant()

                    if (local == null || modifyTime > local.lastModification) {
                        data[name] = T::class.fromJson(result.getString("data"))
                    }
                }
            }
        }
    }

    override suspend fun syncArenas(arenas: MutableMap<String, ArenaConfiguration>) {
        sync(arenas, ARENAS_TABLE)
    }

    override suspend fun syncMaps(maps: MutableMap<String, MapConfiguration>) {
        sync(maps, MAPS_TABLE)
    }

    companion object {

        const val STATS_TABLE = "bedwars_stats"
        const val MAPS_TABLE = "bedwars_maps"
        const val ARENAS_TABLE = "bedwars_arenas"
    }
}