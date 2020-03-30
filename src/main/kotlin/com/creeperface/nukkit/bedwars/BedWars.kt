package com.creeperface.nukkit.bedwars

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.entity.Entity
import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.Config
import cn.nukkit.utils.MainLogger
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.api.BedWarsAPI
import com.creeperface.nukkit.bedwars.api.arena.Arena.ArenaState
import com.creeperface.nukkit.bedwars.api.arena.configuration.ArenaConfiguration
import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.data.provider.DataProvider
import com.creeperface.nukkit.bedwars.api.economy.EconomyProvider
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.arena.config.ConfigurationSerializer
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityMine
import com.creeperface.nukkit.bedwars.command.ReloadCommand
import com.creeperface.nukkit.bedwars.dataprovider.MongoDBDataProvider
import com.creeperface.nukkit.bedwars.dataprovider.MySQLDataProvider
import com.creeperface.nukkit.bedwars.dataprovider.NoneDataProvider
import com.creeperface.nukkit.bedwars.economy.EconomyAPIProvider
import com.creeperface.nukkit.bedwars.economy.NoneEconomyProvider
import com.creeperface.nukkit.bedwars.entity.SpecialItem
import com.creeperface.nukkit.bedwars.entity.Villager
import com.creeperface.nukkit.bedwars.entity.WinParticle
import com.creeperface.nukkit.bedwars.listener.CommandEventListener
import com.creeperface.nukkit.bedwars.listener.EventListener
import com.creeperface.nukkit.bedwars.obj.GlobalData
import com.creeperface.nukkit.bedwars.placeholder.Placeholders
import com.creeperface.nukkit.bedwars.shop.Shop
import com.creeperface.nukkit.bedwars.shop.form.FormShopManager
import com.creeperface.nukkit.bedwars.utils.Configuration
import com.creeperface.nukkit.bedwars.utils.FireworkUtils
import com.creeperface.nukkit.bedwars.utils.identifier
import com.creeperface.nukkit.bedwars.utils.initClass
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaField

class BedWars : PluginBase(), BedWarsAPI {

    val maps = HashMap<String, MapConfiguration>()
    var arenas = HashMap<String, ArenaConfiguration>()

    var ins = HashMap<String, Arena>()

    var players: MutableMap<Long, GlobalData> = HashMap()
    internal val commandListener = CommandEventListener(this)

    var shuttingDown = false

    internal lateinit var configuration: Configuration

    private val economyProviders = mutableMapOf<String, KClass<out EconomyProvider>>()
    private val dataProviders = mutableMapOf<String, KClass<out DataProvider>>()

    override lateinit var economyProvider: EconomyProvider
    override lateinit var dataProvider: DataProvider

    override lateinit var shop: Shop
    val formManager = FormShopManager(this)

    init {
        initInstance()
    }

    override fun onLoad() {
        instance = this

        Entity.registerEntity("SpecialItem", SpecialItem::class.java)
        Entity.registerEntity("BedWarsVillager", Villager::class.java)
        Entity.registerEntity("WinParticle", WinParticle::class.java)

        BlockEntity.registerBlockEntity("BedWarsMine", BlockEntityMine::class.java)

        FireworkUtils.init()

        logger.info("Loading configuration")
        loadConfiguration()
        initDataProviders()
        initEconomyProviders()
    }

    override fun onEnable() {
        initInstance()
        logger.info("Deleting old worlds...")
        deleteOldMaps()

        logger.info("Applying configuration")
        initLanguage()

        loadData()
        loadEconomy()

        this.shop = Shop(this)

        this.registerCommands()
        Placeholders.init(this)

        logger.info("Loading arena configurations")
        this.loadMaps()
        this.loadArenas()
        this.registerArenas()

        this.server.pluginManager.registerEvents(commandListener, this)
        this.server.pluginManager.registerEvents(EventListener(this), this)
    }

    override fun onDisable() {
        shuttingDown = true

        for (arena in this.ins.values) {
            if (arena.gameState == ArenaState.GAME) {
                arena.stopGame()
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
            val cfg = Config(file)
            val arenaConf = ConfigurationSerializer.loadClass(cfg.rootSection, ArenaConfiguration::class)

            arenas[arenaConf.name] = arenaConf
        }
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            val arena = this.getPlayerArena(sender)

            arena?.let {
                when (cmd.name.toLowerCase()) {
                    "stats" -> {

                    }
                    "vote" -> {
                        if (args.size != 1) {
                            sender.sendMessage(prefix + TextFormat.GRAY + "use " + TextFormat.YELLOW + "/vote " + TextFormat.GRAY + "[" + TextFormat.YELLOW + "map" + TextFormat.GRAY + "]")
                            return@let
                        }

                        arena.votingManager.onVote(sender, args[0].toLowerCase())
                    }
                }

                return true
            }


            when (cmd.name.toLowerCase()) {
                "lobby" -> {
                    sender.teleport(this.server.defaultLevel.spawnLocation)
                    sender.inventory.clearAll()
                }
            }
        }

        return true
    }

    override fun getPlayerArena(p: Player): Arena? {
        return players[p.id]?.arena
    }

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

                this.maps[name] = ConfigurationSerializer.loadClass(config.rootSection, MapConfiguration::class)
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

            pathname.name.matches(bedwarsPattern)
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
            if (a.gameState == ArenaState.GAME)
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
        this.configuration = Configuration(this, File(dataFolder, "config.yml"))
    }

    private fun loadEconomy() {
        this.economyProvider = this.economyProviders[this.configuration.economyProvider]?.initClass(configuration, this)
                ?: throw RuntimeException("Undefined economy provider '${this.configuration.economyProvider}'")
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
        val map = server.commandMap
        map.register("reload", ReloadCommand(this))
    }

    private fun initLanguage() {
        val uri = BedWars::class.java.classLoader.getResource("/lang")?.toURI() ?: return

        val path = if (uri.scheme == "jar") {
            FileSystems.newFileSystem(uri, Collections.emptyMap<String, Any>()).getPath("/lang")
        } else {
            Paths.get(uri)
        }

        Files.walk(path, 1).forEach {
            saveResource("lang/" + path.fileName)
        }

        var file = File(dataFolder, "lang/" + configuration.language.toLowerCase() + ".yml")
        if (!file.exists()) {
            logger.error("Language file " + configuration.language.toLowerCase() + ".yml not found, switching to english")
            file = File(dataFolder, "lang/english.yml")
        }

        Lang.init(Config(file, Config.YAML))
    }

    companion object {

        lateinit var instance: BedWars
            private set

        const val prefix = "§l§7[§cBed§fWars§7]§r§f "

        private val bedwarsPattern = Regex("""^.*_bw-[0-9]+$""")
    }
}