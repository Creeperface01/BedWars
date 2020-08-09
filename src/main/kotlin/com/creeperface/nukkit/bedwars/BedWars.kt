package com.creeperface.nukkit.bedwars

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.entity.Entity
import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.Config
import cn.nukkit.utils.ConfigSection
import cn.nukkit.utils.MainLogger
import com.creeperface.nukkit.bedwars.api.BedWarsAPI
import com.creeperface.nukkit.bedwars.api.arena.Arena.ArenaState
import com.creeperface.nukkit.bedwars.api.arena.configuration.ArenaConfiguration
import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.api.data.provider.DataProvider
import com.creeperface.nukkit.bedwars.api.economy.EconomyProvider
import com.creeperface.nukkit.bedwars.api.event.ArenaStopEvent
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.arena.config.ConfigurationSerializer
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityArenaSign
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityMine
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityTeamSign
import com.creeperface.nukkit.bedwars.command.BedWarsCommand
import com.creeperface.nukkit.bedwars.command.ReloadCommand
import com.creeperface.nukkit.bedwars.dataprovider.MongoDBDataProvider
import com.creeperface.nukkit.bedwars.dataprovider.MySQLDataProvider
import com.creeperface.nukkit.bedwars.dataprovider.NoneDataProvider
import com.creeperface.nukkit.bedwars.economy.EconomyAPIProvider
import com.creeperface.nukkit.bedwars.economy.EconomyReward
import com.creeperface.nukkit.bedwars.economy.NoneEconomyProvider
import com.creeperface.nukkit.bedwars.entity.BWVillager
import com.creeperface.nukkit.bedwars.entity.SpecialItem
import com.creeperface.nukkit.bedwars.entity.WinParticle
import com.creeperface.nukkit.bedwars.listener.CommandEventListener
import com.creeperface.nukkit.bedwars.listener.EventListener
import com.creeperface.nukkit.bedwars.manager.FormManager
import com.creeperface.nukkit.bedwars.obj.GlobalData
import com.creeperface.nukkit.bedwars.placeholder.Placeholders
import com.creeperface.nukkit.bedwars.shop.Shop
import com.creeperface.nukkit.bedwars.shop.form.FormShopManager
import com.creeperface.nukkit.bedwars.utils.*
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.joor.Reflect
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaField

class BedWars : PluginBase(), BedWarsAPI {

    val maps = HashMap<String, MapConfiguration>()
    var arenas = HashMap<String, ArenaConfiguration>()

    var ins = HashMap<String, Arena>()

    val players = Long2ObjectOpenHashMap<GlobalData>()
    internal val commandListener = CommandEventListener(this)
    internal val listener = EventListener(this)

    internal lateinit var configuration: Configuration

    private val economyProviders = mutableMapOf<String, KClass<out EconomyProvider>>()
    private val dataProviders = mutableMapOf<String, KClass<out DataProvider>>()

    override lateinit var economyProvider: EconomyProvider
    override lateinit var dataProvider: DataProvider

    override lateinit var shop: Shop

    lateinit var economyRewards: Map<Stat, Collection<EconomyReward>>

    val shopFormManager = FormShopManager(this)
    val formManager = FormManager(this)

//    init {
//        initInstance()
//    }

    override fun onLoad() {
        instance = this
        Reflect.on(logger).set("pluginName", consolePrefix)

        demo {
            logAlert("---------------------------------------")
            logAlert("|   Running demo version of BedWars   |")
            logAlert("|     plugin features are limited     |")
            logAlert("---------------------------------------")
        }

        Entity.registerEntity("SpecialItem", SpecialItem::class.java)
        Entity.registerEntity("BedWarsVillager", BWVillager::class.java)
        Entity.registerEntity("WinParticle", WinParticle::class.java)

        BlockEntity.registerBlockEntity("BedWarsMine", BlockEntityMine::class.java)
        BlockEntity.registerBlockEntity("BedWarsArenaSign", BlockEntityArenaSign::class.java)
        BlockEntity.registerBlockEntity("BedWarsTeamSign", BlockEntityTeamSign::class.java)

        FireworkUtils.init()

        logInfo("Loading configuration")
        loadConfiguration()
        initDataProviders()
        initEconomyProviders()
    }

    override fun onEnable() {
        instance = this
        logInfo("Deleting old worlds...")
        deleteOldMaps()

        logInfo("Applying configuration")
        initLanguage()

        loadData()
        loadEconomy()

        this.shop = Shop(this)
        Placeholders.init(this)

        logInfo("Loading maps")
        this.loadMaps()

        logInfo("Loading arena configurations")
        this.loadArenas()
        this.registerArenas()

        this.registerCommands()

        commandListener.register()
        listener.register()

        printBWLogo()
    }

    override fun onDisable() {
        for (arena in this.ins.values) {
            if (arena.arenaState == ArenaState.GAME) {
                arena.stopGame(ArenaStopEvent.Cause.SHUTDOWN)
            }
        }

        for (data in this.players.values) {
            runBlocking {
                dataProvider.saveData(data.player.identifier, data.stats)
            }
        }

        this.players.clear()

        deleteOldMaps()
        dataProvider.deinit()
    }

    private fun initInstance() {
        BedWarsAPI.Companion::instance.javaField?.let { instance ->
            instance.isAccessible = true
            instance.set(BedWarsAPI.Companion, this)
        }
    }

    private fun registerArenas() {
        arenas.values.forEach {
            this.ins[it.name] = Arena(this, it)
        }
    }

    private fun loadArenas() {
        val dir = File(this.dataFolder, "arenas")
        dir.mkdirs()

        val files = dir.listFiles { f -> f.name.endsWith(".yml") }
        if (files.isNullOrEmpty()) {
            return
        }

        files.forEach { file ->
            val data = configuration.arena.toMutableMap()
            data.putAll(Config(file).rootSection)

            val arenaConf = ConfigurationSerializer.loadClass<ArenaConfiguration>(data)

            arenas[arenaConf.name] = arenaConf

            logInfo("Loaded ${arenaConf.name}")

            demo {
                logAlert("Arena limit is reduced to 1 in demo mode")
                return
            }
        }
    }

    override fun getPlayerArena(p: Player) = players[p.id]?.arena

    override fun getArena(arena: String): Arena? {
        return if (this.ins.containsKey(arena)) {
            this.ins[arena]
        } else null
    }

    private fun loadMaps() {
        try {
            val file = File(this.dataFolder, "maps")
            file.mkdirs()

            val files = file.listFiles { i -> i.name.toLowerCase().endsWith(".yml") }

            if (files.isNullOrEmpty()) {
                return
            }

            for (target in files) {
                val config = Config(target, Config.YAML)
                val name = target.name.substring(0, target.name.length - 4)

                this.maps[name] = ConfigurationSerializer.loadClass(config.rootSection)
                logInfo("Loaded $name")
            }
        } catch (e: Exception) {
            MainLogger.getLogger().logException(e)
        }

    }

    private fun deleteOldMaps() {
        val worlds = File(server.dataPath + "worlds").listFiles(FileFilter { pathname ->
            if (!pathname.isDirectory) {
                return@FileFilter false
            }

            pathname.name.matches(mapPattern)
        })

        if (worlds != null && worlds.isNotEmpty()) {
            for (f in worlds) {
                try {
                    FileUtils.deleteDirectory(f)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    override fun joinRandomArena(p: Player) {
        val a = getFreeArena(p)

        if (a == null) {
            p.sendMessage(Lang.NO_ARENA_FOUND.translate())
            return
        }

        a.joinToArena(p)
    }

    override fun getFreeArena(p: Player): Arena? {
        val pc = p.loginChainData.deviceOS == 7
        val vip = p.hasPermission("bedwars.joinfullarena")

        var arena: Arena? = null
        var players = -1

        for (a in ins.values) {
            if (a.arenaState == ArenaState.GAME)
                continue

            val count = a.playerData.size

            if (!a.multiPlatform && pc || a.multiPlatform && !pc && players > 0) {
                continue
            }

            if (count > players && (vip || count < a.maxPlayers)) {
                arena = a
                players = count
            }
        }

        return arena
    }

    private fun loadConfiguration() {
        saveDefaultConfig()
        saveResource("game.yml")
        saveResource("shop.yml")
        this.configuration = Configuration(this, File(dataFolder, "config.yml"), File(dataFolder, "game.yml"))
    }

    private fun loadEconomy() {
        this.economyProvider = this.economyProviders[this.configuration.economyProvider]?.initClass(configuration, this)
                ?: throw RuntimeException("Undefined economy provider '${this.configuration.economyProvider}'")

        val rewards = mutableMapOf<Stat, Collection<EconomyReward>>()
        configuration.rewards.forEach { (stat, data) ->
            when (data) {
                is ConfigSection -> {
                    val currencyRewards = mutableListOf<EconomyReward>()
                    data.forEach currency@{ currencyName, amount ->
                        val currency = economyProvider.getCurrency(currencyName)

                        if (currency == null) {
                            logWarning("Unknown reward currency $currencyName")
                            return@currency
                        }

                        val count = amount.toString().toDoubleOrNull()

                        if (count == null) {
                            logWarning("Invalid reward amount value ($amount) for stat($stat) and currency($currencyName)")
                            return@currency
                        }

                        currencyRewards.add(EconomyReward(stat, currency, count))
                    }

                    if (currencyRewards.isNotEmpty()) {
                        rewards[stat] = currencyRewards
                    }
                }
                is Number -> {
                    rewards[stat] = listOf(EconomyReward(stat, economyProvider.defaultCurrency, data.toDouble()))
                }
                else -> logWarning("Invalid reward configured for stat($stat)")
            }
        }

        economyRewards = rewards.toMap()
    }

    private fun loadData() {
        this.dataProvider = this.dataProviders[this.configuration.dataProvider]?.initClass(configuration, this)
                ?: throw RuntimeException("Undefined data provider '${this.configuration.dataProvider}'")
    }

    private fun initDataProviders() {
        registerDataProvider("mysql", MySQLDataProvider::class)
        registerDataProvider("mongodb", MongoDBDataProvider::class)
        registerDataProvider("none", NoneDataProvider::class)
    }

    private fun initEconomyProviders() {
        registerEconomyProvider("economyapi", EconomyAPIProvider::class)
        registerEconomyProvider("none", NoneEconomyProvider::class)
    }

    override fun registerDataProvider(name: String, provider: KClass<out DataProvider>) {
        this.dataProviders[name] = provider
    }

    override fun registerEconomyProvider(name: String, provider: KClass<out EconomyProvider>) {
        this.economyProviders[name] = provider
    }

    private fun registerCommands() {
        with(server.commandMap) {
            register("reload", ReloadCommand(this@BedWars))
            register("bedwars", BedWarsCommand(this@BedWars))
        }
    }

    private fun initLanguage() {
//        val uri = BedWars::class.java.classLoader.getResource("lang")?.toURI() ?: error("Could not load language files")
//
//        val path = if (uri.scheme == "jar") {
//            FileSystems.newFileSystem(uri, Collections.emptyMap<String, Any>()).getPath("/lang")
//        } else {
//            Paths.get(uri)
//        }
//
//        Files.walk(path, 2).forEach {
//            logInfo("walk: $it")
//            saveResource("lang/" + path.fileName)
//        }
        val files = arrayOf("czech", "english")

        for (file in files) {
            saveResource("lang/$file.yml")
        }

        val file = with(configuration.language.toLowerCase()) {
            val file = File(dataFolder, "lang/$this.yml")

            if (!file.exists()) {
                logError("Language file $this.yml not found, switching to english")
                File(dataFolder, "lang/english.yml")
            } else {
                file
            }
        }

        Lang.init(Config(file, Config.YAML))
    }

    companion object {

        lateinit var instance: BedWars
            private set

        val prefix: String
            get() = configuration.prefix

        val chatPrefix: String
            get() = configuration.prefix + " "

        const val consolePrefix = "§l§7[§cBed§fWars§7]§r§f "

        private val mapPattern = Regex("""^.*_bw-[0-9]+$""")
    }
}