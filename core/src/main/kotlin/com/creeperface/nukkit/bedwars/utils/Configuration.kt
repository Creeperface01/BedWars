package com.creeperface.nukkit.bedwars.utils

import cn.nukkit.utils.Config
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.api.shop.ShopType
import java.io.File
import java.util.*

internal class Configuration(plugin: BedWars, global: File, game: File) {

    //general
    val language: String
    val prefix: String
    val autoJoin: Boolean
    val disableCommands: Boolean
    val enabledCommands: List<String>
    val separateChat: Boolean

    //chat
    val allPrefix: String
    val lobbyFormat: String
    val teamFormat: String
    val allFormat: String
    val spectatorFormat: String

    //data
    val savePlayerData: Boolean
    val dataProvider: String
    val useDbLib: Boolean
    val loadArenas: Boolean

    val mysql: DB
    val mongo: Mongo

    //economy
    val economyProvider: String
    val rewards = EnumMap<Stat, Any>(Stat::class.java)

    //game
    val shopType: ShopType
    val allowSpectators: Boolean
    val gacNuker: Boolean

    //random
    val synapseTransfer: Boolean

    val playerIdentifier: PlayerIdentifier

    val arena: ConfMap

    init {
        val globalConf = Config(global)
        val gameConf = Config(game).confMap

        with(globalConf.getSection("general")) {
            language = getString("language", "english")
            prefix = getString("prefix")
            autoJoin = getBoolean("auto_join")
            disableCommands = getBoolean("disable_commands")
            enabledCommands = getStringList("enabled_commands")
            separateChat = getBoolean("separate_chat")
        }

        with(globalConf.getSection("data")) {
            savePlayerData = getBoolean("enable")
            dataProvider = getString("data_provider")

            getBoolean("use_db_lib").let {
                useDbLib = if (it && plugin.server.pluginManager.getPlugin("DbLib") == null) {
                    logWarning("'use_db_lib' option is set to true while DbLib plugin not loaded")
                    false
                } else {
                    it
                }
            }

            loadArenas = getBoolean("load_arenas")
            playerIdentifier = try {
                PlayerIdentifier.valueOf(getString("player_identifier").toUpperCase())
            } catch (e: IllegalArgumentException) {
                throw RuntimeException("Undefined player identifier '${getString("player_identifier")}'", e)
            }

            with(getSection("mysql")) {
                mysql = DB(
                    getString("host"),
                    getInt("port"),
                    getString("user"),
                    getString("password"),
                    getString("database")
                )
            }

            with(getSection("mongo")) {
                mongo = Mongo(
                    getString("host"),
                    getInt("port"),
                    getString("user"),
                    getString("password"),
                    getString("database"),
                    getSection("options")
                )
            }
        }

        with(globalConf.getSection("economy")) {
            economyProvider = getString("economy_provider")

            with(getSection("rewards")) {
                this.keys.forEach { key ->
                    try {
                        val stat = Stat.valueOf(key.toUpperCase())
                        rewards[stat] = get(key)
                    } catch (e: IllegalArgumentException) {
                        logError("Invalid reward action $key, skipping")
                    }
                }
            }
        }

        with(globalConf.getSection("synapse")) {
            synapseTransfer = getBoolean("transfer")
        }

        with(gameConf.readSection("general")) {
            shopType = try {
                ShopType.valueOf(readString("shop").toUpperCase())
            } catch (e: IllegalArgumentException) {
                logError("Invalid shop type ${readString("shop")} using default 'inventory'")
                ShopType.INVENTORY
            }

            allowSpectators = readBoolean("allow_spectators")
            gacNuker = readBoolean("nuker_check")
        }

        with(gameConf.readSection("chat")) {
            allPrefix = readString("all_prefix")
            lobbyFormat = readString("lobby_format").replaceColors()
            teamFormat = readString("team_format").replaceColors()
            allFormat = readString("all_format").replaceColors()
            spectatorFormat = readString("spectator_format").replaceColors()
        }

        arena = gameConf.readSection("arena")
    }

    internal class Mongo(
        host: String,
        port: Int,
        user: String,
        password: String,
        database: String,
        val options: Map<String, Any>
    ) : DB(
        host, port, user, password, database
    )

    internal open class DB(
        val host: String,
        val port: Int,
        val user: String,
        val password: String,
        val database: String
    )
}