package com.creeperface.nukkit.bedwars.utils

import cn.nukkit.Player
import cn.nukkit.utils.Config
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.api.economy.EconomyProvider
import java.io.File
import java.util.*

internal class Configuration(plugin: BedWars, file: File) {

    val language: String
    val prefix: String
    val autoJoin: Boolean

    val allPrefix: String
    val teamFormat: String
    val allFormat: String
    val spectatorFormat: String

    val savePlayerData: Boolean
    val dataProvider: String
    val useDbLib: Boolean
    val loadArenas: Boolean

    val mysql: DB
    val mongo: Mongo

    val enableEconomy: Boolean
    val economyProvider: String
    val rewards = EnumMap<Stat, Any>(Stat::class.java)

    val shopType: ShopType
    val votesSize: Int
    val allowSpectators: Boolean

    val synapseTransfer: Boolean

    val playerIdentifier: PlayerIdentifier

    init {
        val conf = Config(file)

        with(conf.getSection("general")) {
            language = getString("language", "english")
            prefix = getString("prefix")
            autoJoin = getBoolean("auto_join")
        }

        with(conf.getSection("chat")) {
            allPrefix = getString("all_prefix")
            teamFormat = getString("team_format")
            allFormat = getString("all_format")
            spectatorFormat = getString("spectator_format")
        }

        with(conf.getSection("data")) {
            savePlayerData = getBoolean("enable")
            dataProvider = getString("data_provider")
            useDbLib = getBoolean("use_db_lib")
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

        with(conf.getSection("economy")) {
            enableEconomy = getBoolean("enable_economy")
            economyProvider = getString("economy_Provider")

            with(getSection("rewards")) {
                this.keys.forEach { key ->
                    try {
                        val stat = Stat.valueOf(key)
                        rewards[stat] = get(key)
                    } catch (e: IllegalArgumentException) {
                        plugin.logger.error("Invalid reward action $key, skipping")
                    }

//                    stat?.let {
//                        val entry = this.get(key)
//
//                        if(entry is Number) {
//
//                        } else if(entry is ConfigSection) {
//
//                        }
//                    }
                }
            }
        }

        with(conf.getSection("game")) {
            shopType = try {
                ShopType.valueOf(getString("shop"))
            } catch (e: IllegalArgumentException) {
                plugin.logger.error("Invalid shop type ${getString("shop")} using default 'inventory'")
                ShopType.INVENTORY
            }

            votesSize = getInt("vote_table_size", 4)
            allowSpectators = getBoolean("allow_spectators")
        }

        with(conf.getSection("synapse")) {
            synapseTransfer = getBoolean("transfer")
        }
    }

    internal class Mongo(host: String, port: Int, user: String, password: String, database: String, val options: Map<String, Any>) : DB(
            host, port, user, password, database
    )

    internal open class DB(
            val host: String,
            val port: Int,
            val user: String,
            val password: String,
            val database: String
    )

    internal class EconomyReward(
            val stat: Stat,
            val currency: EconomyProvider.Currency,
            val amount: Int
    )

    enum class ShopType {
        INVENTORY,
        FORM
    }

    enum class PlayerIdentifier {
        NAME {
            override fun get(player: Player): String = player.name
        },
        UUID {
            override fun get(player: Player): java.util.UUID = player.uniqueId
        };

        abstract fun get(player: Player): Any
    }
}