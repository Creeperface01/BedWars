package bedwars

import bedwars.arena.Arena
import bedwars.blockEntity.BlockEntityMine
import bedwars.entity.EntityAutoJoin
import bedwars.entity.SpecialItem
import bedwars.entity.Villager
import bedwars.entity.WinParticle
import bedwars.mySQL.JoinQuery
import bedwars.mySQL.Stat
import bedwars.mySQL.StatQuery
import bedwars.obj.GlobalData
import bedwars.obj.Language
import bedwars.utils.FireworkUtils
import bedwars.utils.MapUtils
import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.entity.Entity
import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityPortalEnterEvent
import cn.nukkit.event.player.PlayerInteractEntityEvent
import cn.nukkit.event.player.PlayerJoinEvent
import cn.nukkit.event.player.PlayerQuitEvent
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.level.Position
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.Config
import cn.nukkit.utils.MainLogger
import cn.nukkit.utils.TextFormat
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

class BedWars : PluginBase(), Listener {

    var maps = HashMap<String, Map<String, Vector3>>()

    lateinit var level: Level

    lateinit var mainLobby: Position

    var arenas = HashMap<String, HashMap<String, Vector3>>()

    var ins = HashMap<String, Arena>()

    private var loadTime: Long = 0

//    private var queryThread: QueryThread? = null

    var players: MutableMap<Long, GlobalData> = HashMap()

    var shuttingDown = false

    override fun onLoad() {
        instance = this
        loadTime = System.currentTimeMillis()

        Entity.registerEntity("SpecialItem", SpecialItem::class.java)
        Entity.registerEntity("Villager", Villager::class.java)
        Entity.registerEntity("WinParticle", WinParticle::class.java)
        Entity.registerEntity("AutoJoin", EntityAutoJoin::class.java)

        BlockEntity.registerBlockEntity("BedWarsMine", BlockEntityMine::class.java)

        FireworkUtils.init()
    }

    override fun onEnable() {
        //new Thread(new CheckingThread(Thread.currentThread().getId())).start();

        saveDefaultConfig()
        deleteOldMaps()

        initLanguage()

        this.level = this.server.defaultLevel
        this.setMapsData()
        this.setArenasData()
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
            if (arena.game == 1) {
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
        for (name in arenas.keys) {
            registerArena(name)
        }

        /*for(arena arena : ins.values()){
            getServer().getPluginManager().registerEvents(arena, this);
        }*/
    }

    fun registerArena(arena: String) {
        val a = Arena(arena, this)
        this.ins[arena] = a
    }

    fun setArenasData() {
        val bw1 = HashMap<String, Vector3>()
        bw1["sign"] = Vector3(126.0, 50.0, 111.0)
        bw1["1sign"] = Vector3(998.0, 50.0, 1018.0)
        bw1["2sign"] = Vector3(1005.0, 50.0, 1017.0)
        bw1["3sign"] = Vector3(994.0, 50.0, 1017.0)
        bw1["4sign"] = Vector3(1002.0, 50.0, 1018.0)
        bw1["lobby"] = Vector3(1000.0, 50.0, 1000.0)
        this.arenas["bw-1"] = bw1

        val bw2 = HashMap<String, Vector3>()
        bw2["sign"] = Vector3(127.0, 50.0, 111.0)
        bw2["1sign"] = Vector3(998.0, 50.0, 1018.0)
        bw2["2sign"] = Vector3(1005.0, 50.0, 1017.0)
        bw2["3sign"] = Vector3(994.0, 50.0, 1017.0)
        bw2["4sign"] = Vector3(1002.0, 50.0, 1018.0)
        bw2["lobby"] = Vector3(1000.0, 50.0, 1000.0)
        this.arenas["bw-2"] = bw2

        val bw3 = HashMap<String, Vector3>()
        bw3["sign"] = Vector3(128.0, 50.0, 111.0)
        bw3["1sign"] = Vector3(998.0, 50.0, 1018.0)
        bw3["2sign"] = Vector3(1005.0, 50.0, 1017.0)
        bw3["3sign"] = Vector3(994.0, 50.0, 1017.0)
        bw3["4sign"] = Vector3(1002.0, 50.0, 1018.0)
        bw3["lobby"] = Vector3(1000.0, 50.0, 1000.0)
        this.arenas["bw-3"] = bw3

        val bw4 = HashMap<String, Vector3>()
        bw4["sign"] = Vector3(129.0, 50.0, 111.0)
        bw4["1sign"] = Vector3(998.0, 50.0, 1018.0)
        bw4["2sign"] = Vector3(1005.0, 50.0, 1017.0)
        bw4["3sign"] = Vector3(994.0, 50.0, 1017.0)
        bw4["4sign"] = Vector3(1002.0, 50.0, 1018.0)
        bw4["lobby"] = Vector3(1000.0, 50.0, 1000.0)
        this.arenas["bw-4"] = bw4

        val bw5 = HashMap<String, Vector3>()
        bw5["sign"] = Vector3(130.0, 50.0, 111.0)
        bw5["1sign"] = Vector3(998.0, 50.0, 1018.0)
        bw5["2sign"] = Vector3(1005.0, 50.0, 1017.0)
        bw5["3sign"] = Vector3(994.0, 50.0, 1017.0)
        bw5["4sign"] = Vector3(1002.0, 50.0, 1018.0)
        bw5["lobby"] = Vector3(1000.0, 50.0, 1000.0)
        this.arenas["bw-5"] = bw5

        val bw6 = HashMap<String, Vector3>()
        bw6["sign"] = Vector3(126.0, 49.0, 111.0)
        bw6["1sign"] = Vector3(998.0, 50.0, 1018.0)
        bw6["2sign"] = Vector3(1005.0, 50.0, 1017.0)
        bw6["3sign"] = Vector3(994.0, 50.0, 1017.0)
        bw6["4sign"] = Vector3(1002.0, 50.0, 1018.0)
        bw6["lobby"] = Vector3(1000.0, 50.0, 1000.0)
        this.arenas["bw-6"] = bw6

        val bw7 = object : HashMap<String, Vector3>() {
            init {
                put("sign", Vector3(127.0, 49.0, 111.0))
                put("1sign", Vector3(998.0, 50.0, 1018.0))
                put("2sign", Vector3(1005.0, 50.0, 1017.0))
                put("3sign", Vector3(994.0, 50.0, 1017.0))
                put("4sign", Vector3(1002.0, 50.0, 1018.0))
                put("lobby", Vector3(1000.0, 50.0, 1000.0))
            }
        }
        this.arenas["bw-7"] = bw7

        val bw8 = object : HashMap<String, Vector3>() {
            init {
                put("sign", Vector3(128.0, 49.0, 111.0))
                put("1sign", Vector3(998.0, 50.0, 1018.0))
                put("2sign", Vector3(1005.0, 50.0, 1017.0))
                put("3sign", Vector3(994.0, 50.0, 1017.0))
                put("4sign", Vector3(1002.0, 50.0, 1018.0))
                put("lobby", Vector3(1000.0, 50.0, 1000.0))
            }
        }
        this.arenas["bw-8"] = bw8

        val bw9 = object : HashMap<String, Vector3>() {
            init {
                put("sign", Vector3(129.0, 49.0, 111.0))
                put("1sign", Vector3(998.0, 50.0, 1018.0))
                put("2sign", Vector3(1005.0, 50.0, 1017.0))
                put("3sign", Vector3(994.0, 50.0, 1017.0))
                put("4sign", Vector3(1002.0, 50.0, 1018.0))
                put("lobby", Vector3(1000.0, 50.0, 1000.0))
            }
        }
        this.arenas["bw-9"] = bw9

        val bw10 = object : HashMap<String, Vector3>() {
            init {
                put("sign", Vector3(130.0, 49.0, 111.0))
                put("1sign", Vector3(998.0, 50.0, 1018.0))
                put("2sign", Vector3(1005.0, 50.0, 1017.0))
                put("3sign", Vector3(994.0, 50.0, 1017.0))
                put("4sign", Vector3(1002.0, 50.0, 1018.0))
                put("lobby", Vector3(1000.0, 50.0, 1000.0))
            }
        }
        this.arenas["bw-10"] = bw10

        val bw11 = object : HashMap<String, Vector3>() {
            init {
                put("sign", Vector3(126.0, 48.0, 111.0))
                put("1sign", Vector3(998.0, 50.0, 1018.0))
                put("2sign", Vector3(1005.0, 50.0, 1017.0))
                put("3sign", Vector3(994.0, 50.0, 1017.0))
                put("4sign", Vector3(1002.0, 50.0, 1018.0))
                put("lobby", Vector3(1000.0, 50.0, 1000.0))
                put("multiplatform", Vector3())
            }
        }
        this.arenas["bw-11"] = bw11

        val bw12 = object : HashMap<String, Vector3>() {
            init {
                put("sign", Vector3(127.0, 48.0, 111.0))
                put("1sign", Vector3(998.0, 50.0, 1018.0))
                put("2sign", Vector3(1005.0, 50.0, 1017.0))
                put("3sign", Vector3(994.0, 50.0, 1017.0))
                put("4sign", Vector3(1002.0, 50.0, 1018.0))
                put("lobby", Vector3(1000.0, 50.0, 1000.0))
                put("multiplatform", Vector3())
            }
        }
        this.arenas["bw-12"] = bw12

        val bw13 = object : HashMap<String, Vector3>() {
            init {
                put("sign", Vector3(128.0, 48.0, 111.0))
                put("1sign", Vector3(998.0, 50.0, 1018.0))
                put("2sign", Vector3(1005.0, 50.0, 1017.0))
                put("3sign", Vector3(994.0, 50.0, 1017.0))
                put("4sign", Vector3(1002.0, 50.0, 1018.0))
                put("lobby", Vector3(1000.0, 50.0, 1000.0))
                put("multiplatform", Vector3())
            }
        }
        this.arenas["bw-13"] = bw13

        val bw14 = object : HashMap<String, Vector3>() {
            init {
                put("sign", Vector3(129.0, 48.0, 111.0))
                put("1sign", Vector3(998.0, 50.0, 1018.0))
                put("2sign", Vector3(1005.0, 50.0, 1017.0))
                put("3sign", Vector3(994.0, 50.0, 1017.0))
                put("4sign", Vector3(1002.0, 50.0, 1018.0))
                put("lobby", Vector3(1000.0, 50.0, 1000.0))
                put("multiplatform", Vector3())
            }
        }
        this.arenas["bw-14"] = bw14

        val bw15 = object : HashMap<String, Vector3>() {
            init {
                put("sign", Vector3(130.0, 48.0, 111.0))
                put("1sign", Vector3(998.0, 50.0, 1018.0))
                put("2sign", Vector3(1005.0, 50.0, 1017.0))
                put("3sign", Vector3(994.0, 50.0, 1017.0))
                put("4sign", Vector3(1002.0, 50.0, 1018.0))
                put("lobby", Vector3(1000.0, 50.0, 1000.0))
                put("multiplatform", Vector3())
            }
        }
        this.arenas["bw-15"] = bw15

        MapUtils.offsetMap(bw4, 0.0, 500.0)
        MapUtils.offsetMap(bw5, 0.0, 1000.0)
        MapUtils.offsetMap(bw6, 0.0, 1500.0)

        MapUtils.offsetMap(bw9, 500.0, 0.0)
        MapUtils.offsetMap(bw2, 500.0, 500.0)
        MapUtils.offsetMap(bw10, 500.0, 1000.0)
        MapUtils.offsetMap(bw11, 500.0, 1500.0)

        MapUtils.offsetMap(bw3, 1000.0, 1000.0)
        MapUtils.offsetMap(bw12, 1000.0, 500.0)
        MapUtils.offsetMap(bw13, 1000.0, 0.0)


        MapUtils.offsetMap(bw14, 1500.0, 0.0)
        MapUtils.offsetMap(bw15, 1500.0, 500.0)
        MapUtils.offsetMap(bw7, 1500.0, 1000.0)
        MapUtils.offsetMap(bw8, 1500.0, 1500.0)
    }

    fun setMapsData() {
        val kingdoms = HashMap<String, Vector3>()
        //kingdoms.put("world", this.getServer().getLevelByName("Kingdoms"));
        kingdoms["1spawn"] = Vector3(-19.0, 10.0, 386.0)
        kingdoms["1bed"] = Vector3(-21.0, 14.0, 386.0)
        kingdoms["1bed2"] = Vector3(-22.0, 14.0, 386.0)
        kingdoms["1bronze"] = Vector3(-6.0, 8.0, 400.0)
        kingdoms["1iron"] = Vector3(70.0, 9.0, 390.0)
        kingdoms["1gold"] = Vector3(106.0, 9.0, 390.0)
        kingdoms["2spawn"] = Vector3(237.0, 10.0, 394.0)
        kingdoms["2bed"] = Vector3(239.0, 14.0, 394.0)
        kingdoms["2bed2"] = Vector3(240.0, 14.0, 394.0)
        kingdoms["2bronze"] = Vector3(224.0, 8.0, 380.0)
        kingdoms["2iron"] = Vector3(148.0, 9.0, 390.0)
        kingdoms["2gold"] = Vector3(112.0, 9.0, 390.0)
        kingdoms["3spawn"] = Vector3(105.0, 10.0, 518.0)
        kingdoms["3bed"] = Vector3(105.0, 14.0, 520.0)
        kingdoms["3bed2"] = Vector3(105.0, 14.0, 521.0)
        kingdoms["3bronze"] = Vector3(119.0, 8.0, 505.0)
        kingdoms["3iron"] = Vector3(109.0, 9.0, 429.0)
        kingdoms["3gold"] = Vector3(109.0, 9.0, 393.0)
        kingdoms["4spawn"] = Vector3(113.0, 10.0, 262.0)
        kingdoms["4bed"] = Vector3(113.0, 14.0, 260.0)
        kingdoms["4bed2"] = Vector3(113.0, 14.0, 259.0)
        kingdoms["4bronze"] = Vector3(99.0, 8.0, 275.0)
        kingdoms["4iron"] = Vector3(109.0, 9.0, 351.0)
        kingdoms["4gold"] = Vector3(109.0, 9.0, 387.0)
        this.maps["Kingdoms"] = kingdoms

        val chinese = HashMap<String, Vector3>()
        //chinese.put("world", this.getServer().getLevelByName("Chinese"));
        chinese["1spawn"] = Vector3(-1028.0, 121.0, 237.0)
        chinese["1bed"] = Vector3(-1038.0, 121.0, 237.0)
        chinese["1bed2"] = Vector3(-1039.0, 121.0, 237.0)
        chinese["1bronze"] = Vector3(-1032.0, 121.0, 237.0)
        chinese["1iron"] = Vector3(-1022.0, 119.0, 235.0)
        chinese["1gold"] = Vector3(-951.0, 109.0, 238.0)
        chinese["2spawn"] = Vector3(-872.0, 121.0, 237.0)
        chinese["2bed"] = Vector3(-862.0, 121.0, 237.0)
        chinese["2bed2"] = Vector3(-861.0, 121.0, 237.0)
        chinese["2bronze"] = Vector3(-868.0, 121.0, 237.0)
        chinese["2iron"] = Vector3(-878.0, 119.0, 239.0)
        chinese["2gold"] = Vector3(-948.0, 109.0, 237.0)
        chinese["3spawn"] = Vector3(-950.0, 121.0, 315.0)
        chinese["3bed"] = Vector3(-950.0, 121.0, 325.0)
        chinese["3bed2"] = Vector3(-950.0, 121.0, 326.0)
        chinese["3bronze"] = Vector3(-950.0, 121.0, 319.0)
        chinese["3iron"] = Vector3(-952.0, 119.0, 309.0)
        chinese["3gold"] = Vector3(-949.0, 109.0, 239.0)
        chinese["4spawn"] = Vector3(-950.0, 121.0, 159.0)
        chinese["4bed"] = Vector3(-950.0, 121.0, 149.0)
        chinese["4bed2"] = Vector3(-950.0, 121.0, 148.0)
        chinese["4bronze"] = Vector3(-950.0, 121.0, 155.0)
        chinese["4iron"] = Vector3(-948.0, 118.0, 165.0)
        chinese["4gold"] = Vector3(-950.0, 108.0, 236.0)
        this.maps["Chinese"] = chinese

        val phizzle = HashMap<String, Vector3>()
        //phizzle.put("world", this.getServer().getLevelByName("Phizzle"));
        phizzle["1spawn"] = Vector3(-6.0, 111.0, 1.0)
        phizzle["1bed"] = Vector3(0.0, 111.0, -4.0)
        phizzle["1bed2"] = Vector3(-1.0, 111.0, -4.0)
        phizzle["1bronze"] = Vector3(-8.0, 111.0, 4.0)
        phizzle["1iron"] = Vector3(-9.0, 110.0, -5.0)
        phizzle["1gold"] = Vector3(-1.0, 111.0, 53.0)
        phizzle["2spawn"] = Vector3(51.0, 111.0, 56.0)
        phizzle["2bed"] = Vector3(56.0, 111.0, 62.0)
        phizzle["2bed2"] = Vector3(56.0, 111.0, 61.0)
        phizzle["2bronze"] = Vector3(48.0, 111.0, 54.0)
        phizzle["2iron"] = Vector3(57.0, 110.0, 53.0)
        phizzle["2gold"] = Vector3(-1.0, 111.0, 61.0)
        phizzle["3spawn"] = Vector3(-61.0, 111.0, 58.0)
        phizzle["3bed"] = Vector3(-66.0, 111.0, 53.0)
        phizzle["3bed2"] = Vector3(-66.0, 111.0, 52.0)
        phizzle["3bronze"] = Vector3(-58.0, 111.0, 60.0)
        phizzle["3iron"] = Vector3(-67.0, 110.0, 61.0)
        phizzle["3gold"] = Vector3(-10.0, 111.0, 53.0)
        phizzle["4spawn"] = Vector3(-4.0, 111.0, 113.0)
        phizzle["4bed"] = Vector3(-9.0, 111.0, 118.0)
        phizzle["4bed2"] = Vector3(-10.0, 111.0, 118.0)
        phizzle["4bronze"] = Vector3(-2.0, 111.0, 110.0)
        phizzle["4iron"] = Vector3(-1.0, 110.0, 119.0)
        phizzle["4gold"] = Vector3(-10.0, 111.0, 61.0)
        this.maps["Phizzle"] = phizzle

        val stw5 = HashMap<String, Vector3>()
        //stw5.put("world", this.getServer().getLevelByName("STW5"));
        stw5["1spawn"] = Vector3(-349.0, 35.0, 257.0)
        stw5["1bed"] = Vector3(-330.0, 38.0, 255.0)
        stw5["1bed2"] = Vector3(-330.0, 38.0, 254.0)
        stw5["1bronze"] = Vector3(-345.0, 34.0, 260.0)
        stw5["1iron"] = Vector3(-346.0, 34.0, 214.0)
        stw5["1gold"] = Vector3(-339.0, 40.0, 181.0)
        stw5["2spawn"] = Vector3(-343.0, 35.0, 91.0)
        stw5["2bed"] = Vector3(-362.0, 38.0, 93.0)
        stw5["2bed2"] = Vector3(-362.0, 38.0, 94.0)
        stw5["2bronze"] = Vector3(-347.0, 34.0, 88.0)
        stw5["2iron"] = Vector3(-346.0, 34.0, 134.0)
        stw5["2gold"] = Vector3(-353.0, 40.0, 167.0)
        stw5["3spawn"] = Vector3(-429.0, 35.0, 171.0)
        stw5["3bed"] = Vector3(-427.0, 38.0, 190.0)
        stw5["3bed2"] = Vector3(-426.0, 38.0, 190.0)
        stw5["3bronze"] = Vector3(-432.0, 34.0, 175.0)
        stw5["3iron"] = Vector3(-386.0, 34.0, 174.0)
        stw5["3gold"] = Vector3(-353.0, 40.0, 181.0)
        stw5["4spawn"] = Vector3(-263.0, 35.0, 177.0)
        stw5["4bed"] = Vector3(-265.0, 38.0, 158.0)
        stw5["4bed2"] = Vector3(-266.0, 38.0, 158.0)
        stw5["4bronze"] = Vector3(-260.0, 34.0, 173.0)
        stw5["4iron"] = Vector3(-306.0, 34.0, 174.0)
        stw5["4gold"] = Vector3(-339.0, 40.0, 167.0)
        this.maps["STW5"] = stw5

        val bw1 = HashMap<String, Vector3>()
        //bw1.put("world", this.getServer().getLevelByName("BedWars1"));
        bw1["1spawn"] = Vector3(-1267.0, 98.0, -981.0)
        bw1["1bed"] = Vector3(-1267.0, 102.0, -986.0)
        bw1["1bed2"] = Vector3(-1267.0, 102.0, -985.0)
        bw1["1bronze"] = Vector3(-1267.0, 98.0, -983.0)
        bw1["1iron"] = Vector3(-1302.0, 98.0, -950.0)
        bw1["1gold"] = Vector3(-1267.0, 98.0, -917.0)
        bw1["2spawn"] = Vector3(-1267.0, 98.0, -849.0)
        bw1["2bed"] = Vector3(-1267.0, 102.0, -844.0)
        bw1["2bed2"] = Vector3(-1267.0, 102.0, -845.0)
        bw1["2bronze"] = Vector3(-1267.0, 98.0, -847.0)
        bw1["2iron"] = Vector3(-1232.0, 98.0, -880.0)
        bw1["2gold"] = Vector3(-1267.0, 98.0, -913.0)
        bw1["3spawn"] = Vector3(-1333.0, 98.0, -915.0)
        bw1["3bed"] = Vector3(-1338.0, 102.0, -915.0)
        bw1["3bed2"] = Vector3(-1337.0, 102.0, -915.0)
        bw1["3bronze"] = Vector3(-1335.0, 98.0, -915.0)
        bw1["3iron"] = Vector3(-1302.0, 98.0, -880.0)
        bw1["3gold"] = Vector3(-1269.0, 98.0, -915.0)
        bw1["4spawn"] = Vector3(-1201.0, 98.0, -915.0)
        bw1["4bed"] = Vector3(-1196.0, 102.0, -915.0)
        bw1["4bed2"] = Vector3(-1197.0, 102.0, -915.0)
        bw1["4bronze"] = Vector3(-1199.0, 98.0, -915.0)
        bw1["4iron"] = Vector3(-1232.0, 98.0, -950.0)
        bw1["4gold"] = Vector3(-1265.0, 98.0, -915.0)
        this.maps["BedWars1"] = bw1

        val bw2 = HashMap<String, Vector3>()
        //bw2.put("world", this.getServer().getLevelByName("BedWars2"));
        bw2["1spawn"] = Vector3(353.0, 39.0, 630.0)
        bw2["1bed"] = Vector3(353.0, 39.0, 627.0)
        bw2["1bed2"] = Vector3(353.0, 39.0, 626.0)
        bw2["1bronze"] = Vector3(353.0, 39.0, 640.0)
        bw2["1iron"] = Vector3(351.0, 39.0, 639.0)
        bw2["1gold"] = Vector3(354.0, 40.0, 540.0)
        bw2["2spawn"] = Vector3(353.0, 39.0, 446.0)
        bw2["2bed"] = Vector3(353.0, 39.0, 449.0)
        bw2["2bed2"] = Vector3(353.0, 39.0, 450.0)
        bw2["2bronze"] = Vector3(353.0, 39.0, 436.0)
        bw2["2iron"] = Vector3(355.0, 39.0, 437.0)
        bw2["2gold"] = Vector3(351.0, 40.0, 536.0)
        bw2["3spawn"] = Vector3(445.0, 39.0, 538.0)
        bw2["3bed"] = Vector3(442.0, 39.0, 538.0)
        bw2["3bed2"] = Vector3(441.0, 39.0, 538.0)
        bw2["3bronze"] = Vector3(455.0, 39.0, 538.0)
        bw2["3iron"] = Vector3(454.0, 39.0, 540.0)
        bw2["3gold"] = Vector3(355.0, 40.0, 536.0)
        bw2["4spawn"] = Vector3(261.0, 39.0, 538.0)
        bw2["4bed"] = Vector3(264.0, 39.0, 538.0)
        bw2["4bed2"] = Vector3(265.0, 39.0, 538.0)
        bw2["4bronze"] = Vector3(251.0, 39.0, 538.0)
        bw2["4iron"] = Vector3(252.0, 39.0, 536.0)
        bw2["4gold"] = Vector3(351.0, 40.0, 540.0)
        this.maps["BedWars2"] = bw2

        val nether = HashMap<String, Vector3>()
        //nether.put("world", this.getServer().getLevelByName("Nether"));
        nether["1spawn"] = Vector3(178.0, 64.0, 746.0)
        nether["1bed"] = Vector3(178.0, 71.0, 735.0)
        nether["1bed2"] = Vector3(178.0, 71.0, 734.0)
        nether["1bronze"] = Vector3(174.0, 64.0, 740.0)
        nether["1iron"] = Vector3(182.0, 64.0, 740.0)
        nether["1gold"] = Vector3(178.0, 65.0, 793.0)
        nether["2spawn"] = Vector3(230.0, 64.0, 798.0)
        nether["2bed"] = Vector3(241.0, 71.0, 798.0)
        nether["2bed2"] = Vector3(242.0, 71.0, 798.0)
        nether["2bronze"] = Vector3(236.0, 64.0, 794.0)
        nether["2iron"] = Vector3(236.0, 64.0, 802.0)
        nether["2gold"] = Vector3(182.0, 65.0, 799.0)
        nether["3spawn"] = Vector3(178.0, 64.0, 850.0)
        nether["3bed"] = Vector3(178.0, 71.0, 861.0)
        nether["3bed2"] = Vector3(178.0, 71.0, 862.0)
        nether["3bronze"] = Vector3(182.0, 64.0, 856.0)
        nether["3iron"] = Vector3(174.0, 64.0, 856.0)
        nether["3gold"] = Vector3(178.0, 65.0, 803.0)
        nether["4spawn"] = Vector3(126.0, 64.0, 798.0)
        nether["4bed"] = Vector3(115.0, 71.0, 798.0)
        nether["4bed2"] = Vector3(114.0, 71.0, 798.0)
        nether["4bronze"] = Vector3(120.0, 64.0, 802.0)
        nether["4iron"] = Vector3(120.0, 64.0, 794.0)
        nether["4gold"] = Vector3(173.0, 65.0, 799.0)
        this.maps["Nether"] = nether

        loadMapsFromConfig()
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
                    "blue" -> {
                        if (arena.game == 1) {
                            return@let
                        }
                        arena.addToTeam(sender, 1)
                    }
                    "red" -> {
                        if (arena.game == 1) {
                            return@let
                        }
                        arena.addToTeam(sender, 2)
                    }
                    "yellow" -> {
                        if (arena.game == 1) {
                            return@let
                        }
                        arena.addToTeam(sender, 3)
                    }
                    "green" -> {
                        if (arena.game == 1) {
                            return@let
                        }
                        arena.addToTeam(sender, 4)
                    }
                    "lobby" -> {
                        arena.leaveArena(sender)
                        sender.inventory.clearAll()
                    }
                    "stats" -> {
                        val data = this.players[sender.id] ?: return@let

                        val stats = data.stats

                        sender.sendMessage(Language.translate("stats", stats[Stat.KILLS].toString(), stats[Stat.DEATHS].toString(), stats[Stat.WINS].toString(), stats[Stat.LOSSES].toString(), stats[Stat.BEDS].toString()))
                    }
                    "bw" -> {
                        if (!sender.isOp) {
                            return@let
                        }
                        val c = args.size
                        if (c < 1) {
                            return false
                        }
                        when (args[0]) {
                            "start" -> arena.selectMap(true)
                            "stop" -> arena.stopGame()
                        }//arena.startGame();
                    }
                    "vote" -> {
                        if (args.size != 1) {
                            sender.sendMessage(BedWars.prefix + TextFormat.GRAY + "use " + TextFormat.YELLOW + "/vote " + TextFormat.GRAY + "[" + TextFormat.YELLOW + "map" + TextFormat.GRAY + "]")
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
                "bw" -> {
                    if (!sender.isOp)
                        return true

                    if (args.isEmpty())
                        return true

                    when (args[0].toLowerCase()) {
                        "joinentity" -> {

                            val nbt = CompoundTag()
                                    .putList(ListTag<DoubleTag>("Pos")
                                            .add(DoubleTag("", sender.x))
                                            .add(DoubleTag("", sender.y))
                                            .add(DoubleTag("", sender.z)))
                                    .putList(ListTag<DoubleTag>("Motion")
                                            .add(DoubleTag("", 0.0))
                                            .add(DoubleTag("", 0.0))
                                            .add(DoubleTag("", 0.0)))
                                    .putList(ListTag<FloatTag>("Rotation")
                                            .add(FloatTag("", 0.toFloat()))
                                            .add(FloatTag("", 0.toFloat())))

                            val join = EntityAutoJoin(sender.chunk, nbt)
                            join.spawnToAll()
                        }
                    }
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

    private fun loadMapsFromConfig() {
        try {
            val file = File(this.dataFolder, "maps")
            file.mkdirs()

            val files = file.listFiles { i -> i.name.toLowerCase().endsWith(".yml") }

            if (files == null || files.size == 0) {
                return
            }

            for (target in files) {
                val config = Config(target, Config.YAML)
                val name = target.name.substring(0, target.name.length - 4)

                this.maps[name] = MapUtils.loadMap(config)
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

    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        val entity = e.entity

//        if (entity is Player && e.cause == DamageCause.VOID && entity.getLevel().id == MTCore.getInstance().level.id && this.getPlayerArena(entity) == null) {
//            entity.teleport(MTCore.getInstance().lobby)
//        } else
        if (entity is EntityAutoJoin && e is EntityDamageByEntityEvent) {
            val damager = e.damager

            if (damager is Player) {
                if (getPlayerArena(damager) != null) {
                    return
                }

                handleJoinNPCClick(damager)
            }
        }
    }

    @EventHandler
    fun onEntityClick(e: PlayerInteractEntityEvent) {
        val p = e.player
        val entity = e.entity

        if (entity is EntityAutoJoin) {
            if (getPlayerArena(p) != null) {
                return
            }

            handleJoinNPCClick(p)
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

    fun handleJoinNPCClick(p: Player) {
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
            if (a.game > 0)
                continue

            val count = a.playerData.size

            if (!a.isMultiPlatform && pc || a.isMultiPlatform && !pc && players > 0) {
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