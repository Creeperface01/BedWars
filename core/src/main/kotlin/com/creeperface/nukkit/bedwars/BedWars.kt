package com.creeperface.nukkit.bedwars

import cn.nukkit.Player
import cn.nukkit.block.BlockChest
import cn.nukkit.block.BlockSandstone
import cn.nukkit.block.BlockTNT
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.entity.Entity
import cn.nukkit.item.*
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.level.Level
import cn.nukkit.level.Position
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.Config
import cn.nukkit.utils.ConfigSection
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
import com.creeperface.nukkit.bedwars.entity.TNTShip
import com.creeperface.nukkit.bedwars.entity.Villager
import com.creeperface.nukkit.bedwars.entity.WinParticle
import com.creeperface.nukkit.bedwars.listener.CommandEventListener
import com.creeperface.nukkit.bedwars.listener.EventListener
import com.creeperface.nukkit.bedwars.obj.GlobalData
import com.creeperface.nukkit.bedwars.placeholder.Placeholders
import com.creeperface.nukkit.bedwars.shop.ItemWindow
import com.creeperface.nukkit.bedwars.shop.ShopWindow
import com.creeperface.nukkit.bedwars.shop.Window
import com.creeperface.nukkit.bedwars.utils.*
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.jvm.javaField

class BedWars : PluginBase(), BedWarsAPI {

    val maps = HashMap<String, MapConfiguration>()

    lateinit var level: Level

    lateinit var mainLobby: Position

    var arenas = HashMap<String, ArenaConfiguration>()

    var ins = HashMap<String, Arena>()

    private var loadTime: Long = 0

    var players: MutableMap<Long, GlobalData> = HashMap()
    internal val commandListener = CommandEventListener(this)

    var shuttingDown = false

    internal lateinit var configuration: Configuration

    private val economyProviders = mutableMapOf<String, EconomyProvider>()
    private val dataProviders = mutableMapOf<String, DataProvider>()

    override lateinit var economyProvider: EconomyProvider
    override lateinit var dataProvider: DataProvider

    init {
        initInstance()
    }

    override fun onLoad() {
        instance = this
        loadTime = System.currentTimeMillis()

        Entity.registerEntity("SpecialItem", SpecialItem::class.java)
        Entity.registerEntity("BedWarsVillager", Villager::class.java)
        Entity.registerEntity("WinParticle", WinParticle::class.java)

        BlockEntity.registerBlockEntity("BedWarsMine", BlockEntityMine::class.java)

        FireworkUtils.init()

        initDataProviders()
        initEconomyProviders()
    }

    override fun onEnable() {
        initInstance()
        logger.info("Deleting old worlds...")
        deleteOldMaps()

        logger.info("Loading configuration")
        loadConfiguration()
        initLanguage()

        loadData()
        loadEconomy()

        this.registerCommands()
        Placeholders.init(this)

        logger.info("Loading arena configurations")
        this.level = this.server.defaultLevel
        this.loadMaps()
        this.loadArenas()
        this.registerArenas()
        this.mainLobby = this.level.spawnLocation

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
                    sender.teleport(this.mainLobby)
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
        this.economyProvider = this.economyProviders[this.configuration.economyProvider]
                ?: throw RuntimeException("Undefined economy provider '${this.configuration.economyProvider}'")
    }

    private fun loadData() {
        this.dataProvider = this.dataProviders[this.configuration.dataProvider]
                ?: throw RuntimeException("Undefined data provider '${this.configuration.dataProvider}'")
    }

    private fun initDataProviders() {
        registerDataProvider("mysql", MySQLDataProvider(configuration))
        registerDataProvider("mongodb", MongoDBDataProvider(configuration))
        registerDataProvider("none", NoneDataProvider)
    }

    private fun initEconomyProviders() {
        registerEconomyProvider("economyapi", EconomyAPIProvider(configuration))
        registerEconomyProvider("none", NoneEconomyProvider)
    }

    override fun registerDataProvider(name: String, provider: DataProvider) {
        this.dataProviders[name] = provider
    }

    override fun registerEconomyProvider(name: String, provider: EconomyProvider) {
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

    fun convertShop() {
        val main = ItemWindow(true)

        val blocksW = ItemWindow()
        val armorW = ItemWindow()
        val pickaxeW = ItemWindow()
        val swordW = ItemWindow()
        val bowW = ItemWindow()
        val foodW = ItemWindow()
        val chestW = ItemWindow()
        val potionW = ItemWindow()
        val specialW = ItemWindow()

        val blocks = LinkedHashMap<Item, Window>()
        blocks[Item.get(Item.SANDSTONE)] = ShopWindow(Item.get(Item.SANDSTONE, 0, 2), Items.BRONZE.clone(), blocksW)
        blocks[Item.get(Item.SANDSTONE)] = ShopWindow(Item.get(Item.SANDSTONE, 0, 16), Items.BRONZE.setCountR(8), blocksW)
        blocks[Item.get(Item.END_STONE)] = ShopWindow(Item.get(Item.END_STONE), Items.BRONZE.setCountR(7), blocksW)
        blocks[Item.get(Item.GLOWSTONE_BLOCK)] = ShopWindow(Item.get(Item.GLOWSTONE_BLOCK, 0, 4), Items.BRONZE.setCountR(15), blocksW)
        blocks[Item.get(Item.IRON_BLOCK)] = ShopWindow(Item.get(Item.IRON_BLOCK), Items.IRON.setCountR(1), blocksW)
        blocks[Item.get(Item.GLASS)] = ShopWindow(Item.get(Item.GLASS), Items.BRONZE.setCountR(4), blocksW)

        val armor = LinkedHashMap<Item, Window>()
        armor[ItemHelmetLeather().setCompoundTag(CompoundTag().putInt("customColor", 0)).addEnchantment(Enchantment.ID_DURABILITY, 1)] = ShopWindow(ItemHelmetLeather().setCompoundTag(CompoundTag().putInt("customColor", 0)).addEnchantment(Enchantment.ID_DURABILITY, 1), Items.BRONZE.clone(), armorW)
        armor[ItemLeggingsLeather().setCompoundTag(CompoundTag().putInt("customColor", 0)).addEnchantment(Enchantment.ID_DURABILITY, 1)] = ShopWindow(ItemLeggingsLeather().setCompoundTag(CompoundTag().putInt("customColor", 0)).addEnchantment(Enchantment.ID_DURABILITY, 1), Items.BRONZE.clone(), armorW)
        armor[ItemBootsLeather().setCompoundTag(CompoundTag().putInt("customColor", 0)).addEnchantment(Enchantment.ID_DURABILITY, 1)] = ShopWindow(ItemBootsLeather().setCompoundTag(CompoundTag().putInt("customColor", 0)).addEnchantment(Enchantment.ID_DURABILITY, 1), Items.BRONZE.clone(), armorW)
        armor[ItemChestplateChain().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_PROTECTION_ALL, 1).setCustomName("Chestplate lvl I")] = ShopWindow(ItemChestplateChain().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_PROTECTION_ALL, 1).setCustomName("Chestplate lvl I"), Items.IRON.setCountR(1), armorW)
        armor[ItemChestplateChain().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_PROTECTION_ALL, 2).setCustomName("Chestplate lvl II")] = ShopWindow(ItemChestplateChain().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_PROTECTION_ALL, 2).setCustomName("Chestplate lvl II"), Items.IRON.setCountR(3), armorW)
        armor[ItemChestplateChain().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_PROTECTION_ALL, 3).setCustomName("Chestplate lvl III")] = ShopWindow(ItemChestplateChain().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_PROTECTION_ALL, 3).setCustomName("Chestplate lvl III"), Items.IRON.setCountR(7), armorW)

        val pickaxes = LinkedHashMap<Item, Window>()
        pickaxes[ItemPickaxeWood().addEnchantment(Enchantment.ID_EFFICIENCY, 1).addEnchantment(Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl I")] = ShopWindow(ItemPickaxeWood().addEnchantment(Enchantment.ID_EFFICIENCY, 1).addEnchantment(Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl I"), Items.BRONZE.setCountR(4), pickaxeW)
        pickaxes[ItemPickaxeStone().addEnchantment(Enchantment.ID_EFFICIENCY, 1).addEnchantment(Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl II")] = ShopWindow(ItemPickaxeStone().addEnchantment(Enchantment.ID_EFFICIENCY, 1).addEnchantment(Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl II"), Items.IRON.setCountR(2), pickaxeW)
        pickaxes[ItemPickaxeIron().addEnchantment(Enchantment.ID_EFFICIENCY, 3).addEnchantment(Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl III")] = ShopWindow(ItemPickaxeIron().addEnchantment(Enchantment.ID_EFFICIENCY, 3).addEnchantment(Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl III"), Items.GOLD.clone(), pickaxeW)

        val swords = LinkedHashMap<Item, Window>()
        swords[ItemStick().addEnchantment(Enchantment.ID_KNOCKBACK, 1).setCustomName("Knockback Stick")] = ShopWindow(ItemStick().addEnchantment(Enchantment.ID_KNOCKBACK, 1).setCustomName("Knockback Stick"), Items.BRONZE.setCountR(8), swordW)
        swords[ItemSwordGold().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_DAMAGE_ALL, 1).setCustomName("Sword lvl I")] = ShopWindow(ItemSwordGold().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_DAMAGE_ALL, 1).setCustomName("Sword lvl I"), Items.IRON.clone(), swordW)
        swords[ItemSwordGold().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_DAMAGE_ALL, 2).setCustomName("Sword lvl II")] = ShopWindow(ItemSwordGold().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_DAMAGE_ALL, 2).setCustomName("Sword lvl II"), Items.IRON.setCountR(3), swordW)
        swords[ItemSwordIron().addEnchantment(Enchantment.ID_KNOCKBACK, 1).addEnchantment(Enchantment.ID_DAMAGE_ALL, 2).setCustomName("Sword lvl III").addEnchantment(Enchantment.ID_DURABILITY, 2)] = ShopWindow(ItemSwordIron().addEnchantment(Enchantment.ID_KNOCKBACK, 1).addEnchantment(Enchantment.ID_DAMAGE_ALL, 2).addEnchantment(Enchantment.ID_DURABILITY, 2).setCustomName("Sword lvl III"), Items.GOLD.setCountR(5), swordW)

        val bows = LinkedHashMap<Item, Window>()
        bows[ItemBow().addEnchantment(Enchantment.ID_BOW_INFINITY, 1).setCustomName("Bow lvl I")] = ShopWindow(ItemBow().addEnchantment(Enchantment.ID_BOW_INFINITY, 1), Items.GOLD.setCountR(3), bowW)
        bows[ItemBow().addEnchantment(Enchantment.ID_BOW_INFINITY, 1).addEnchantment(Enchantment.ID_BOW_POWER, 1).setCustomName("Bow lvl II")] = ShopWindow(ItemBow().addEnchantment(Enchantment.ID_BOW_INFINITY, 1).addEnchantment(Enchantment.ID_BOW_POWER, 1).setCustomName("Bow lvl II"), Items.GOLD.setCountR(7), bowW)
        bows[ItemBow().addEnchantment(Enchantment.ID_BOW_INFINITY, 1).addEnchantment(Enchantment.ID_BOW_POWER, 1).addEnchantment(Enchantment.ID_BOW_KNOCKBACK, 1).setCustomName("Bow lvl III")] = ShopWindow(ItemBow().addEnchantment(Enchantment.ID_BOW_INFINITY, 1).addEnchantment(Enchantment.ID_BOW_POWER, 1).addEnchantment(Enchantment.ID_BOW_KNOCKBACK, 1).setCustomName("Bow lvl III"), Items.GOLD.setCountR(13), bowW)
        bows[ItemBow().addEnchantment(Enchantment.ID_BOW_INFINITY, 1).setCustomName("Explosive Bow")] = ShopWindow(ItemBow().addEnchantment(Enchantment.ID_BOW_INFINITY, 1).setCustomName("Explosive Bow"), Items.GOLD.setCountR(20), bowW)
        bows[ItemArrow()] = ShopWindow(ItemArrow(), Items.GOLD.clone(), bowW)

        val food = LinkedHashMap<Item, Window>()
        food[ItemApple()] = ShopWindow(ItemApple(), Items.BRONZE.clone(), foodW)
        food[ItemPorkchopCooked()] = ShopWindow(ItemPorkchopCooked(), Items.BRONZE.setCountR(2), foodW)
        food[ItemCake()] = ShopWindow(ItemCake(), Items.IRON.clone(), foodW)
        food[ItemAppleGold()] = ShopWindow(ItemAppleGold(), Items.GOLD.setCountR(3), foodW)

        val chests = LinkedHashMap<Item, Window>()
        chests[Item.get(Item.CHEST)] = ShopWindow(Item.get(Item.CHEST), Items.IRON.clone(), chestW)
        chests[Item.get(Item.ENDER_CHEST)] = ShopWindow(Item.get(Item.ENDER_CHEST), Items.GOLD.clone(), chestW)

        val potions = LinkedHashMap<Item, Window>()
        potions[ItemPotion(ItemPotion.INSTANT_HEALTH)] = ShopWindow(ItemPotion(ItemPotion.INSTANT_HEALTH), Items.IRON.setCountR(3), potionW)
        potions[ItemPotion(ItemPotion.INSTANT_HEALTH_II)] = ShopWindow(ItemPotion(ItemPotion.INSTANT_HEALTH_II), Items.IRON.setCountR(5), potionW)
        potions[ItemPotion(ItemPotion.SPEED_LONG)] = ShopWindow(ItemPotion(ItemPotion.SPEED_LONG), Items.IRON.setCountR(7), potionW)
        potions[ItemPotion(ItemPotion.STRENGTH_LONG)] = ShopWindow(ItemPotion(ItemPotion.STRENGTH_LONG), Items.GOLD.setCountR(8), potionW)

        val specials = LinkedHashMap<Item, Window>()
        specials[Item.get(Item.SPONGE).setCustomName(TextFormat.GOLD.toString() + "Lucky Block")] = ShopWindow(Item.get(Item.SPONGE).setCustomName(TextFormat.GOLD.toString() + "Lucky Block"), Items.IRON.setCountR(5), specialW)
        specials[Item.get(Item.ENDER_PEARL)] = ShopWindow(Item.get(Item.ENDER_PEARL), Items.GOLD.setCountR(13), specialW)
        specials[Item.get(Item.SPAWN_EGG, TNTShip.NETWORK_ID).setCustomName("${TextFormat.RED}Sheepy ${TextFormat.GOLD}Sheep ${TextFormat.YELLOW}Sheep")] = ShopWindow(Item.get(Item.SPAWN_EGG, TNTShip.NETWORK_ID).setCustomName("${TextFormat.RED}Sheepy ${TextFormat.GOLD}Sheep ${TextFormat.YELLOW}Sheep"), Items.BRONZE.setCountR(64), specialW)
        specials[Item.get(Item.STONE_PRESSURE_PLATE).setCustomName(TextFormat.GOLD.toString() + "Mine")] = ShopWindow(Item.get(Item.STONE_PRESSURE_PLATE).setCustomName(TextFormat.GOLD.toString() + "Mine"), Items.IRON.setCountR(5), specialW)
        specials[Item.get(Item.FISHING_ROD)] = ShopWindow(Item.get(Item.FISHING_ROD), Items.IRON.setCountR(2), specialW)
        //TODO: WARP DUST

        ////////////////////////////////////////////////////////////////////

        blocksW.setWindows(blocks, main)
        armorW.setWindows(armor, main)
        pickaxeW.setWindows(pickaxes, main)
        swordW.setWindows(swords, main)
        foodW.setWindows(food, main)
        bowW.setWindows(bows, main)
        chestW.setWindows(chests, main)
        potionW.setWindows(potions, main)
        specialW.setWindows(specials, main)

        val mainWindow = LinkedHashMap<Item, Window>()
        mainWindow[ItemBlock(BlockSandstone()).setCustomName("${TextFormat.YELLOW}Blocks")] = blocksW
        mainWindow[ItemChestplateChain().setCustomName("${TextFormat.YELLOW}Armor")] = armorW
        mainWindow[ItemSwordGold().setCustomName("${TextFormat.YELLOW}Swords")] = swordW
        mainWindow[ItemPickaxeStone().setCustomName("${TextFormat.YELLOW}Pickaxes")] = pickaxeW
        mainWindow[ItemBow().setCustomName("${TextFormat.YELLOW}Bows")] = bowW
        mainWindow[ItemBlock(BlockChest()).setCustomName("${TextFormat.YELLOW}Chests")] = chestW
        mainWindow[ItemApple().setCustomName("${TextFormat.YELLOW}Food")] = foodW
        mainWindow[ItemPotion().setCustomName("${TextFormat.YELLOW}Potions")] = potionW
        mainWindow[ItemBlock(BlockTNT()).setCustomName("${TextFormat.YELLOW}Special")] = specialW

        main.setWindows(mainWindow)

        val shop = ArrayList<ConfigSection>()

        fun process(item: Item, window: Window): ConfigSection {
            val section = ConfigSection()
            section["name"] = window.name

            val icon = ConfigSection()
            icon["item_id"] = item.id
            icon["item_damage"] = item.damage

            if (item.hasCustomName())
                icon["item_custom_name"] = item.customName

            item.lore?.let { lore ->
                if (lore.isEmpty())
                    return@let

                icon["lore"] = lore
            }

            item.enchantments?.let { enchants ->
                if (enchants.isEmpty())
                    return@let

                val ench = enchants.map {
                    val sec = ConfigSection()

                    sec["id"] = it.id
                    sec["level"] = it.level

                    sec
                }

                icon["enchantments"] = ench
            }

            icon["item_path"] = item.name

            section["icon"] = icon

            if (window is ItemWindow) {
                section["type"] = "menu"

                val children = ArrayList<ConfigSection>()

                for (i in 0 until window.size) {
                    val subItem = window.getItem(i)
                    val subWindow = window.getWindow(i)

                    if (subItem.isNull || subWindow == null) {
                        continue
                    }

                    children.add(process(subItem, subWindow))
                }

                section["children"] = children
            } else if (window is ShopWindow) {
                section["type"] = "shop"
            }

            return section
        }

        main.windows.forEach { (_, window) -> shop.add(process(Item.get(0), window)) }

        val cfg = Config(Config.YAML)
        cfg["section"] = shop

        cfg.save(File(dataFolder, "shop.yml"))
    }

    companion object {

        lateinit var instance: BedWars
            private set

        const val prefix = "§l§7[§cBed§fWars§7]§r§f "

        private val bedwarsPattern = Regex("""^.*_bw-[0-9]+$""")
    }
}