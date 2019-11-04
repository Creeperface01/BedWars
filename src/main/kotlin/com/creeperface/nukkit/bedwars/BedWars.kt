package com.creeperface.nukkit.bedwars

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.entity.Entity
import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.entity.EntityPortalEnterEvent
import cn.nukkit.event.player.PlayerJoinEvent
import cn.nukkit.event.player.PlayerQuitEvent
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.level.Position
import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.Config
import cn.nukkit.utils.MainLogger
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.arena.config.ArenaConfiguration
import com.creeperface.nukkit.bedwars.arena.config.MapConfiguration
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityMine
import com.creeperface.nukkit.bedwars.entity.SpecialItem
import com.creeperface.nukkit.bedwars.entity.Villager
import com.creeperface.nukkit.bedwars.entity.WinParticle
import com.creeperface.nukkit.bedwars.mysql.JoinQuery
import com.creeperface.nukkit.bedwars.mysql.Stat
import com.creeperface.nukkit.bedwars.mysql.StatQuery
import com.creeperface.nukkit.bedwars.obj.GlobalData
import com.creeperface.nukkit.bedwars.obj.Language
import com.creeperface.nukkit.bedwars.utils.ConfigurationSerializer
import com.creeperface.nukkit.bedwars.utils.FireworkUtils
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

class BedWars : PluginBase(), Listener {

    val maps = HashMap<String, MapConfiguration>()

    lateinit var level: Level

    lateinit var mainLobby: Position

    var arenas = HashMap<String, ArenaConfiguration>()

    var ins = HashMap<String, Arena>()

    private var loadTime: Long = 0

//    private var queryThread: QueryThread? = null

    var players: MutableMap<Long, GlobalData> = HashMap()

    var shuttingDown = false

    override fun onLoad() {
        instance = this
        loadTime = System.currentTimeMillis()

        Entity.registerEntity("SpecialItem", SpecialItem::class.java)
        Entity.registerEntity("BedWarsVillager", Villager::class.java)
        Entity.registerEntity("WinParticle", WinParticle::class.java)

        BlockEntity.registerBlockEntity("BedWarsMine", BlockEntityMine::class.java)

        FireworkUtils.init()
    }

    override fun onEnable() {
        //new Thread(new CheckingThread(Thread.currentThread().getId())).start();

        saveDefaultConfig()
        deleteOldMaps()

        initLanguage()

        this.level = this.server.defaultLevel
        this.loadMaps()
        this.loadArenas()
        this.registerArenas()
        this.mainLobby = this.level.spawnLocation
        this.server.pluginManager.registerEvents(this, this)
        this.level.time = 5000
        this.level.stopTime()
        //entity.registerEntity("TNTShip", TNTShip.class);
        Item.addCreativeItem(Item.get(Item.SPAWN_EGG, 15))

//        this.queryThread = QueryThread()
//        this.queryThread!!.start()
    }

    override fun onDisable() {
        shuttingDown = true

        for (arena in this.ins.values) {
            if (arena.game == Arena.ArenaState.GAME) {
                arena.stopGame()
            }
        }

        for (data in this.players.values) {
            StatQuery(this, data.stats)
        }

        this.players.clear()

//        refreshQuery(false)
        deleteOldMaps()
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

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val p = e.player
        this.players[p.id] = GlobalData(p)

        JoinQuery(this, p)
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val data = this.players.remove(e.player.id) ?: return

        StatQuery(this, data.stats)
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            val arena = this.getPlayerArena(sender)

            arena?.let {
                when (cmd.name.toLowerCase()) {
                    "lobby" -> {
                        arena.leaveArena(sender)
                        sender.inventory.clearAll()
                    }
                    "stats" -> {
                        val data = this.players[sender.id] ?: return@let

                        val stats = data.stats

                        sender.sendMessage(Language.translate("stats", stats[Stat.KILLS].toString(), stats[Stat.DEATHS].toString(), stats[Stat.WINS].toString(), stats[Stat.LOSSES].toString(), stats[Stat.BEDS].toString()))
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
                    sender.teleport(this.mainLobby)
                    sender.inventory.clearAll()
                }
            }
        }

        return true
    }

    fun getPlayerArena(p: Player): Arena? {
        return players[p.id]?.arena
    }

    fun getArena(arena: String): Arena? {
        return if (this.ins.containsKey(arena)) {
            this.ins[arena]
        } else null
    }

    /*@EventHandler
    public void onInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();

        if(!p.isOp()){
            return;
        }

        Item item = p.getInventory().getItemInHand();

        if(item.getId() == Item.SPAWN_EGG && item.getDamage() ==)
    }*/

    private fun initLanguage() {
        val languages = arrayOf("english", "czech")

        var lang = config.getString("language").toLowerCase()

        if (lang.isBlank() || !languages.contains(lang)) {
            logger.warning("Language $lang doesn't exist")
            lang = "english"
        }

        val langs = HashMap<String, Config>()

        languages.forEach {
            saveResource("$it.yml", false)
            langs[it] = Config("$dataFolder/$it.yml")
        }

        Language.init(langs, lang)
    }

//    fun refreshQuery(async: Boolean): HashMap<String, String> {
//        val data = object : HashMap<String, String>() {
//            init {
//                put("id", id)
//                put("players", "" + server.onlinePlayers.size)
//                put("maxplayers", "" + 200)
//
//                val game: String
//
//                if (!MTCore.isShuttingDown) {
//                    game = TextFormat.GREEN.toString() + "Lobby"
//                } else {
//                    game = TextFormat.BLACK.toString() + "Restarting..."
//                }
//
//                put("line1", TextFormat.DARK_RED.toString() + "> " + id + " <")
//                put("line2", "")
//                put("line3", "" + TextFormat.BLACK + server.onlinePlayers.size + "/200")
//                put("line4", game)
//            }
//        }
//
//        if (MTCore.isShuttingDown) {
//            QueryRefreshTask(data, false)
//        }
//
//        return data
//    }

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

            val matcher = bedwarsPattern.matcher(pathname.name)

            matcher.matches()
        })

        if (worlds != null && worlds.isNotEmpty()) {
            for (f in worlds) {
                try {
                    FileUtils.deleteDirectory(f)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            /*List<String> names = new ArrayList<>();

            for(File f : worlds) {
                names.add(f.getName());
            }

            System.out.println(Arrays.toString(names.stream().toArray(String[]::new)));*/
        }
    }

    //@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
//    fun onLevelChange(e: EntityLevelChangeEvent) {
//        if (e.entity is Player) {
//            val p = e.entity as Player
//            val level = e.target
//
//            val data = GTGadgets.getData(p) ?: return
//
//            if (level.id != server.defaultLevel.id) {
//                data.disable(AddonType.COSTUME)
//                data.disable(AddonType.GADGET)
//                data.disable(AddonType.PET)
//                data.disable(AddonType.RIDING)
//            } else {
//                data.enable(AddonType.COSTUME)
//                data.enable(AddonType.GADGET)
//                data.enable(AddonType.PET)
//                data.enable(AddonType.RIDING)
//            }
//        }
//    }

    fun joinRandomArena(p: Player) {
        val a = getFreeArena(p)

        if (a == null) {
            p.sendMessage(Language.translate("no_arena_found"))
            return
        }

        a.joinToArena(p)
    }

    private fun getFreeArena(p: Player): Arena? {
        val pc = p.loginChainData.deviceOS == 7
        val vip = p.hasPermission("gameteam.vip")

        var arena: Arena? = null
        var players = -1

        for (a in ins.values) {
            if (a.game == Arena.ArenaState.GAME)
                continue

            val count = a.playerData.size

            if (!a.multiPlatform && pc || a.multiPlatform && !pc && players > 0) {
                continue
            }

            if (count > players && (vip || count < Arena.MAX_PLAYERS)) {
                arena = a
                players = count
            }
        }

        return arena
    }

    @EventHandler
    fun onPortalEnter(e: EntityPortalEnterEvent) {
        e.setCancelled()
    }

    companion object {

        lateinit var instance: BedWars
            private set

        val prefix: String
            get() = "§l§7[§cBed§fWars§7] §r§f "

        private val bedwarsPattern = Pattern.compile("^.*_bw-[0-9]+$")
    }
}