package com.creeperface.nukkit.bedwars.arena

import cn.nukkit.block.BlockChest
import cn.nukkit.block.BlockSandstone
import cn.nukkit.block.BlockTNT
import cn.nukkit.item.*
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.api.arena.Arena.ArenaState
import com.creeperface.nukkit.bedwars.api.arena.Team
import com.creeperface.nukkit.bedwars.api.arena.configuration.IArenaConfiguration
import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.entity.TNTShip
import com.creeperface.nukkit.bedwars.obj.BedWarsData
import com.creeperface.nukkit.bedwars.shop.inventory.MenuWindow
import com.creeperface.nukkit.bedwars.shop.inventory.OfferWindow
import com.creeperface.nukkit.bedwars.shop.inventory.Window
import com.creeperface.nukkit.bedwars.utils.EnderChestInventory
import com.creeperface.nukkit.bedwars.utils.Items
import com.creeperface.nukkit.bedwars.utils.addEnchantment
import com.creeperface.nukkit.bedwars.utils.setCountR
import java.util.*

class Team(var arena: Arena,
           val id: Int,
           config: IArenaConfiguration.TeamConfiguration
) : Team, IArenaConfiguration.ITeamConfiguration by config {

    private var bed = true

    var status = ""
        private set

    override val enderChest = EnderChestInventory()

    lateinit var shop: MenuWindow
        private set

    val players = mutableMapOf<String, BedWarsData>()

    lateinit var mapConfig: MapConfiguration.TeamData

    init {
        recalculateStatus()

        registerShop()
    }

    override fun getTeamPlayers() = players.toMap()

    override fun hasBed() = this.bed

    override fun messagePlayers(message: String) {
        for (p in ArrayList(this.players.values)) {
            p.player.sendMessage(message)
        }
        this.arena.plugin.server.logger.info(message)
    }

    fun messagePlayers(message: String, data: BedWarsData) {
        val player = data.player

        val msg = TextFormat.GRAY.toString() + "[" + chatColor + "Team" + TextFormat.GRAY + "]   " + player.displayName /*+ data.baseData.chatColor*/ + ": " + message //TODO: chat color

        for (p in ArrayList(this.players.values)) {
            p.player.sendMessage(msg)
        }
        this.arena.plugin.server.logger.info(msg)
    }

    fun addPlayer(p: BedWarsData) {
        this.players[p.player.name.toLowerCase()] = p
        p.team = this
        p.player.nameTag = chatColor.toString() + p.player.name
        p.player.displayName = TextFormat.GRAY.toString() /*+ "[" + TextFormat.GREEN + p.baseData.level + TextFormat.GRAY + "]" + p.baseData.prefix*/ + " " + p.team.chatColor + p.player.name + TextFormat.RESET //TODO: chat color
        recalculateStatus()
    }

    fun removePlayer(data: BedWarsData) {
        this.players.remove(data.player.name.toLowerCase())
        data.player.nameTag = data.player.name
        recalculateStatus()

        if (arena.gameState == ArenaState.GAME) {
            arena.scoreboardManager.updateTeam(this.id)
        }
    }

    fun onBedBreak() {
        this.bed = false
        recalculateStatus()
        arena.scoreboardManager.updateTeam(this.id)
    }

    private fun recalculateStatus() {
        val count = this.players.size
        val bed = hasBed()

        if (count >= 1 || bed) {
            this.status = "                                          " + chatColor + name + ": " + (if (bed) TextFormat.GREEN.toString() + "✔" else TextFormat.RED.toString() + "✖") + TextFormat.GRAY + " " + this.players.size + "\n"
        } else {
            this.status = ""
        }
    }

    /**
     * shop
     */

    private fun registerShop() {
        val main = MenuWindow(true)

        val blocksW = MenuWindow()
        val armorW = MenuWindow()
        val pickaxeW = MenuWindow()
        val swordW = MenuWindow()
        val bowW = MenuWindow()
        val foodW = MenuWindow()
        val chestW = MenuWindow()
        val potionW = MenuWindow()
        val specialW = MenuWindow()

        val blocks = LinkedHashMap<Item, Window>()
        blocks[Item.get(Item.SANDSTONE)] = OfferWindow(Item.get(Item.SANDSTONE, 0, 2), Items.BRONZE.clone(), blocksW)
        blocks[Item.get(Item.SANDSTONE)] = OfferWindow(Item.get(Item.SANDSTONE, 0, 16), Items.BRONZE.setCountR(8), blocksW)
        blocks[Item.get(Item.END_STONE)] = OfferWindow(Item.get(Item.END_STONE), Items.BRONZE.setCountR(7), blocksW)
        blocks[Item.get(Item.GLOWSTONE_BLOCK)] = OfferWindow(Item.get(Item.GLOWSTONE_BLOCK, 0, 4), Items.BRONZE.setCountR(15), blocksW)
        blocks[Item.get(Item.IRON_BLOCK)] = OfferWindow(Item.get(Item.IRON_BLOCK), Items.IRON.setCountR(1), blocksW)
        blocks[Item.get(Item.GLASS)] = OfferWindow(Item.get(Item.GLASS), Items.BRONZE.setCountR(4), blocksW)

        val armor = LinkedHashMap<Item, Window>()
        armor[ItemHelmetLeather().setColor(color).addEnchantment(Enchantment.ID_DURABILITY, 1)] = OfferWindow(ItemHelmetLeather().setColor(color).addEnchantment(Enchantment.ID_DURABILITY, 1), Items.BRONZE.clone(), armorW)
        armor[ItemLeggingsLeather().setColor(color).addEnchantment(Enchantment.ID_DURABILITY, 1)] = OfferWindow(ItemLeggingsLeather().setColor(color).addEnchantment(Enchantment.ID_DURABILITY, 1), Items.BRONZE.clone(), armorW)
        armor[ItemBootsLeather().setColor(color).addEnchantment(Enchantment.ID_DURABILITY, 1)] = OfferWindow(ItemBootsLeather().setColor(color).addEnchantment(Enchantment.ID_DURABILITY, 1), Items.BRONZE.clone(), armorW)
        armor[ItemChestplateChain().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_PROTECTION_ALL, 1).setCustomName("Chestplate lvl I")] = OfferWindow(ItemChestplateChain().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_PROTECTION_ALL, 1).setCustomName("Chestplate lvl I"), Items.IRON.setCountR(1), armorW)
        armor[ItemChestplateChain().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_PROTECTION_ALL, 2).setCustomName("Chestplate lvl II")] = OfferWindow(ItemChestplateChain().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_PROTECTION_ALL, 2).setCustomName("Chestplate lvl II"), Items.IRON.setCountR(3), armorW)
        armor[ItemChestplateChain().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_PROTECTION_ALL, 3).setCustomName("Chestplate lvl III")] = OfferWindow(ItemChestplateChain().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_PROTECTION_ALL, 3).setCustomName("Chestplate lvl III"), Items.IRON.setCountR(7), armorW)

        val pickaxes = LinkedHashMap<Item, Window>()
        pickaxes[ItemPickaxeWood().addEnchantment(Enchantment.ID_EFFICIENCY, 1).addEnchantment(Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl I")] = OfferWindow(ItemPickaxeWood().addEnchantment(Enchantment.ID_EFFICIENCY, 1).addEnchantment(Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl I"), Items.BRONZE.setCountR(4), pickaxeW)
        pickaxes[ItemPickaxeStone().addEnchantment(Enchantment.ID_EFFICIENCY, 1).addEnchantment(Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl II")] = OfferWindow(ItemPickaxeStone().addEnchantment(Enchantment.ID_EFFICIENCY, 1).addEnchantment(Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl II"), Items.IRON.setCountR(2), pickaxeW)
        pickaxes[ItemPickaxeIron().addEnchantment(Enchantment.ID_EFFICIENCY, 3).addEnchantment(Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl III")] = OfferWindow(ItemPickaxeIron().addEnchantment(Enchantment.ID_EFFICIENCY, 3).addEnchantment(Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl III"), Items.GOLD.clone(), pickaxeW)

        val swords = LinkedHashMap<Item, Window>()
        swords[ItemStick().addEnchantment(Enchantment.ID_KNOCKBACK, 1).setCustomName("Knockback Stick")] = OfferWindow(ItemStick().addEnchantment(Enchantment.ID_KNOCKBACK, 1).setCustomName("Knockback Stick"), Items.BRONZE.setCountR(8), swordW)
        swords[ItemSwordGold().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_DAMAGE_ALL, 1).setCustomName("Sword lvl I")] = OfferWindow(ItemSwordGold().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_DAMAGE_ALL, 1).setCustomName("Sword lvl I"), Items.IRON.clone(), swordW)
        swords[ItemSwordGold().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_DAMAGE_ALL, 2).setCustomName("Sword lvl II")] = OfferWindow(ItemSwordGold().addEnchantment(Enchantment.ID_DURABILITY, 1).addEnchantment(Enchantment.ID_DAMAGE_ALL, 2).setCustomName("Sword lvl II"), Items.IRON.setCountR(3), swordW)
        swords[ItemSwordIron().addEnchantment(Enchantment.ID_KNOCKBACK, 1).addEnchantment(Enchantment.ID_DAMAGE_ALL, 2).setCustomName("Sword lvl III").addEnchantment(Enchantment.ID_DURABILITY, 2)] = OfferWindow(ItemSwordIron().addEnchantment(Enchantment.ID_KNOCKBACK, 1).addEnchantment(Enchantment.ID_DAMAGE_ALL, 2).addEnchantment(Enchantment.ID_DURABILITY, 2).setCustomName("Sword lvl III"), Items.GOLD.setCountR(5), swordW)

        val bows = LinkedHashMap<Item, Window>()
        bows[ItemBow().addEnchantment(Enchantment.ID_BOW_INFINITY, 1).setCustomName("Bow lvl I")] = OfferWindow(ItemBow().addEnchantment(Enchantment.ID_BOW_INFINITY, 1), Items.GOLD.setCountR(3), bowW)
        bows[ItemBow().addEnchantment(Enchantment.ID_BOW_INFINITY, 1).addEnchantment(Enchantment.ID_BOW_POWER, 1).setCustomName("Bow lvl II")] = OfferWindow(ItemBow().addEnchantment(Enchantment.ID_BOW_INFINITY, 1).addEnchantment(Enchantment.ID_BOW_POWER, 1).setCustomName("Bow lvl II"), Items.GOLD.setCountR(7), bowW)
        bows[ItemBow().addEnchantment(Enchantment.ID_BOW_INFINITY, 1).addEnchantment(Enchantment.ID_BOW_POWER, 1).addEnchantment(Enchantment.ID_BOW_KNOCKBACK, 1).setCustomName("Bow lvl III")] = OfferWindow(ItemBow().addEnchantment(Enchantment.ID_BOW_INFINITY, 1).addEnchantment(Enchantment.ID_BOW_POWER, 1).addEnchantment(Enchantment.ID_BOW_KNOCKBACK, 1).setCustomName("Bow lvl III"), Items.GOLD.setCountR(13), bowW)
        bows[ItemBow().addEnchantment(Enchantment.ID_BOW_INFINITY, 1).setCustomName("Explosive Bow")] = OfferWindow(ItemBow().addEnchantment(Enchantment.ID_BOW_INFINITY, 1).setCustomName("Explosive Bow"), Items.GOLD.setCountR(20), bowW)
        bows[ItemArrow()] = OfferWindow(ItemArrow(), Items.GOLD.clone(), bowW)

        val food = LinkedHashMap<Item, Window>()
        food[ItemApple()] = OfferWindow(ItemApple(), Items.BRONZE.clone(), foodW)
        food[ItemPorkchopCooked()] = OfferWindow(ItemPorkchopCooked(), Items.BRONZE.setCountR(2), foodW)
        food[ItemCake()] = OfferWindow(ItemCake(), Items.IRON.clone(), foodW)
        food[ItemAppleGold()] = OfferWindow(ItemAppleGold(), Items.GOLD.setCountR(3), foodW)

        val chests = LinkedHashMap<Item, Window>()
        chests[Item.get(Item.CHEST)] = OfferWindow(Item.get(Item.CHEST), Items.IRON.clone(), chestW)
        chests[Item.get(Item.ENDER_CHEST)] = OfferWindow(Item.get(Item.ENDER_CHEST), Items.GOLD.clone(), chestW)

        val potions = LinkedHashMap<Item, Window>()
        potions[ItemPotion(ItemPotion.INSTANT_HEALTH)] = OfferWindow(ItemPotion(ItemPotion.INSTANT_HEALTH), Items.IRON.setCountR(3), potionW)
        potions[ItemPotion(ItemPotion.INSTANT_HEALTH_II)] = OfferWindow(ItemPotion(ItemPotion.INSTANT_HEALTH_II), Items.IRON.setCountR(5), potionW)
        potions[ItemPotion(ItemPotion.SPEED_LONG)] = OfferWindow(ItemPotion(ItemPotion.SPEED_LONG), Items.IRON.setCountR(7), potionW)
        potions[ItemPotion(ItemPotion.STRENGTH_LONG)] = OfferWindow(ItemPotion(ItemPotion.STRENGTH_LONG), Items.GOLD.setCountR(8), potionW)

        val specials = LinkedHashMap<Item, Window>()
        specials[Item.get(Item.SPONGE).setCustomName(TextFormat.GOLD.toString() + "Lucky Block")] = OfferWindow(Item.get(Item.SPONGE).setCustomName(TextFormat.GOLD.toString() + "Lucky Block"), Items.IRON.setCountR(5), specialW)
        specials[Item.get(Item.ENDER_PEARL)] = OfferWindow(Item.get(Item.ENDER_PEARL), Items.GOLD.setCountR(13), specialW)
        specials[Item.get(Item.SPAWN_EGG, TNTShip.NETWORK_ID).setCustomName("${TextFormat.RED}Sheepy ${TextFormat.GOLD}Sheep ${TextFormat.YELLOW}Sheep")] = OfferWindow(Item.get(Item.SPAWN_EGG, TNTShip.NETWORK_ID).setCustomName("${TextFormat.RED}Sheepy ${TextFormat.GOLD}Sheep ${TextFormat.YELLOW}Sheep"), Items.BRONZE.setCountR(64), specialW)
        specials[Item.get(Item.STONE_PRESSURE_PLATE).setCustomName(TextFormat.GOLD.toString() + "Mine")] = OfferWindow(Item.get(Item.STONE_PRESSURE_PLATE).setCustomName(TextFormat.GOLD.toString() + "Mine"), Items.IRON.setCountR(5), specialW)
        specials[Item.get(Item.FISHING_ROD)] = OfferWindow(Item.get(Item.FISHING_ROD), Items.IRON.setCountR(2), specialW)
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

        this.shop = main
    }
}
