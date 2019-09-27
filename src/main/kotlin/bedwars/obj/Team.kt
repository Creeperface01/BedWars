package bedwars.obj

import bedwars.arena.Arena
import bedwars.entity.TNTShip
import bedwars.shop.ItemWindow
import bedwars.shop.ShopWindow
import bedwars.shop.Window
import bedwars.utils.EnderChestInventory
import bedwars.utils.Items
import cn.nukkit.block.BlockChest
import cn.nukkit.block.BlockSandstone
import cn.nukkit.block.BlockTNT
import cn.nukkit.item.*
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.utils.DyeColor
import cn.nukkit.utils.TextFormat
import java.util.*

class Team(var arena: Arena,
           var id: Int,
           var name: String,
           var color: TextFormat,
           var decimal: Int) {

    var bed = true

    val dyeColor: DyeColor = DyeColor.valueOf(this.color.name.toUpperCase())

    var status = ""
        private set

    val enderChest = EnderChestInventory()

    lateinit var shop: ItemWindow
        private set

    val players = mutableMapOf<String, BedWarsData>()

    lateinit var spawn: Vector3

    init {
        recalculateStatus()

        registerShop()
    }

    fun hasBed(): Boolean {
        return this.bed
    }

    fun messagePlayers(message: String) {
        for (p in ArrayList(this.players.values)) {
            p.player.sendMessage(message)
        }
        this.arena.plugin.server.logger.info(message)
    }

    fun messagePlayers(message: String, data: BedWarsData) {
        val player = data.player

        val msg = TextFormat.GRAY.toString() + "[" + color + "Team" + TextFormat.GRAY + "]   " + player.displayName /*+ data.baseData.chatColor*/ + ": " + message //TODO: chat color

        for (p in ArrayList(this.players.values)) {
            p.player.sendMessage(msg)
        }
        this.arena.plugin.server.logger.info(msg)
    }

    fun addPlayer(p: BedWarsData) {
        this.players[p.player.name.toLowerCase()] = p
        p.team = this
        p.player.nameTag = color.toString() + p.player.name
        p.player.displayName = TextFormat.GRAY.toString() /*+ "[" + TextFormat.GREEN + p.baseData.level + TextFormat.GRAY + "]" + p.baseData.prefix*/ + " " + p.team!!.color + p.player.name + TextFormat.RESET //TODO: chat color
        recalculateStatus()
    }

    fun removePlayer(p: BedWarsData) {
        this.players.remove(p.player.name.toLowerCase())
        p.team = null
        p.player.nameTag = p.player.name
        recalculateStatus()

        if (arena.game > 0) {
            arena.barUtil.updateTeamStats()
        }
    }

    fun onBedBreak() {
        this.bed = false
        recalculateStatus()
        arena.barUtil.updateTeamStats()
    }

    fun recalculateStatus() {
        val count = this.players.size
        val bed = hasBed()

        if (count >= 1 || bed) {
            this.status = "                                          " + color + name + ": " + (if (bed) TextFormat.GREEN.toString() + "✔" else TextFormat.RED.toString() + "✖") + TextFormat.GRAY + " " + this.players.size + "\n"
        } else {
            this.status = ""
        }
    }

    /**
     * shop
     */

    private fun registerShop() {
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
        blocks[Item.get(Item.SANDSTONE)] = ShopWindow(Item.get(Item.SANDSTONE, 0, 16), setCount(Items.BRONZE, 8), blocksW)
        blocks[Item.get(Item.END_STONE)] = ShopWindow(Item.get(Item.END_STONE), setCount(Items.BRONZE, 7), blocksW)
        blocks[Item.get(Item.GLOWSTONE_BLOCK)] = ShopWindow(Item.get(Item.GLOWSTONE_BLOCK, 0, 4), setCount(Items.BRONZE, 15), blocksW)
        blocks[Item.get(Item.IRON_BLOCK)] = ShopWindow(Item.get(Item.IRON_BLOCK), setCount(Items.IRON, 1), blocksW)
        blocks[Item.get(Item.GLASS)] = ShopWindow(Item.get(Item.GLASS), setCount(Items.BRONZE, 4), blocksW)

        val armor = LinkedHashMap<Item, Window>()
        armor[addEnchantment(ItemHelmetLeather().setCompoundTag(CompoundTag().putInt("customColor", decimal)), Enchantment.ID_DURABILITY, 1)] = ShopWindow(addEnchantment(ItemHelmetLeather().setCompoundTag(CompoundTag().putInt("customColor", decimal)), Enchantment.ID_DURABILITY, 1), Items.BRONZE.clone(), armorW)
        armor[addEnchantment(ItemLeggingsLeather().setCompoundTag(CompoundTag().putInt("customColor", decimal)), Enchantment.ID_DURABILITY, 1)] = ShopWindow(addEnchantment(ItemLeggingsLeather().setCompoundTag(CompoundTag().putInt("customColor", decimal)), Enchantment.ID_DURABILITY, 1), Items.BRONZE.clone(), armorW)
        armor[addEnchantment(ItemBootsLeather().setCompoundTag(CompoundTag().putInt("customColor", decimal)), Enchantment.ID_DURABILITY, 1)] = ShopWindow(addEnchantment(ItemBootsLeather().setCompoundTag(CompoundTag().putInt("customColor", decimal)), Enchantment.ID_DURABILITY, 1), Items.BRONZE.clone(), armorW)
        armor[addEnchantment(addEnchantment(ItemChestplateChain(), Enchantment.ID_DURABILITY, 1), Enchantment.ID_PROTECTION_ALL, 1).setCustomName("Chestplate lvl I")] = ShopWindow(addEnchantment(addEnchantment(ItemChestplateChain(), Enchantment.ID_DURABILITY, 1), Enchantment.ID_PROTECTION_ALL, 1).setCustomName("Chestplate lvl I"), setCount(Items.IRON, 1), armorW)
        armor[addEnchantment(addEnchantment(ItemChestplateChain(), Enchantment.ID_DURABILITY, 1), Enchantment.ID_PROTECTION_ALL, 2).setCustomName("Chestplate lvl II")] = ShopWindow(addEnchantment(addEnchantment(ItemChestplateChain(), Enchantment.ID_DURABILITY, 1), Enchantment.ID_PROTECTION_ALL, 2).setCustomName("Chestplate lvl II"), setCount(Items.IRON, 3), armorW)
        armor[addEnchantment(addEnchantment(ItemChestplateChain(), Enchantment.ID_DURABILITY, 1), Enchantment.ID_PROTECTION_ALL, 3).setCustomName("Chestplate lvl III")] = ShopWindow(addEnchantment(addEnchantment(ItemChestplateChain(), Enchantment.ID_DURABILITY, 1), Enchantment.ID_PROTECTION_ALL, 3).setCustomName("Chestplate lvl III"), setCount(Items.IRON, 7), armorW)

        val pickaxes = LinkedHashMap<Item, Window>()
        pickaxes[addEnchantment(addEnchantment(ItemPickaxeWood(), Enchantment.ID_EFFICIENCY, 1), Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl I")] = ShopWindow(addEnchantment(addEnchantment(ItemPickaxeWood(), Enchantment.ID_EFFICIENCY, 1), Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl I"), setCount(Items.BRONZE, 4), pickaxeW)
        pickaxes[addEnchantment(addEnchantment(ItemPickaxeStone(), Enchantment.ID_EFFICIENCY, 1), Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl II")] = ShopWindow(addEnchantment(addEnchantment(ItemPickaxeStone(), Enchantment.ID_EFFICIENCY, 1), Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl II"), setCount(Items.IRON, 2), pickaxeW)
        pickaxes[addEnchantment(addEnchantment(ItemPickaxeIron(), Enchantment.ID_EFFICIENCY, 3), Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl III")] = ShopWindow(addEnchantment(addEnchantment(ItemPickaxeIron(), Enchantment.ID_EFFICIENCY, 3), Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl III"), Items.GOLD.clone(), pickaxeW)

        val swords = LinkedHashMap<Item, Window>()
        swords[addEnchantment(ItemStick(), Enchantment.ID_KNOCKBACK, 1).setCustomName("Knockback Stick")] = ShopWindow(addEnchantment(ItemStick(), Enchantment.ID_KNOCKBACK, 1).setCustomName("Knockback Stick"), setCount(Items.BRONZE, 8), swordW)
        swords[addEnchantment(addEnchantment(ItemSwordGold(), Enchantment.ID_DURABILITY, 1), Enchantment.ID_DAMAGE_ALL, 1).setCustomName("Sword lvl I")] = ShopWindow(addEnchantment(addEnchantment(ItemSwordGold(), Enchantment.ID_DURABILITY, 1), Enchantment.ID_DAMAGE_ALL, 1).setCustomName("Sword lvl I"), Items.IRON.clone(), swordW)
        swords[addEnchantment(addEnchantment(ItemSwordGold(), Enchantment.ID_DURABILITY, 1), Enchantment.ID_DAMAGE_ALL, 2).setCustomName("Sword lvl II")] = ShopWindow(addEnchantment(addEnchantment(ItemSwordGold(), Enchantment.ID_DURABILITY, 1), Enchantment.ID_DAMAGE_ALL, 2).setCustomName("Sword lvl II"), setCount(Items.IRON, 3), swordW)
        swords[addEnchantment(addEnchantment(addEnchantment(ItemSwordIron(), Enchantment.ID_KNOCKBACK, 1), Enchantment.ID_DAMAGE_ALL, 2).setCustomName("Sword lvl III"), Enchantment.ID_DURABILITY, 2)] = ShopWindow(addEnchantment(addEnchantment(addEnchantment(ItemSwordIron(), Enchantment.ID_KNOCKBACK, 1), Enchantment.ID_DAMAGE_ALL, 2), Enchantment.ID_DURABILITY, 2).setCustomName("Sword lvl III"), setCount(Items.GOLD, 5), swordW)

        val bows = LinkedHashMap<Item, Window>()
        bows[addEnchantment(ItemBow(), Enchantment.ID_BOW_INFINITY, 1).setCustomName("Bow lvl I")] = ShopWindow(addEnchantment(ItemBow(), Enchantment.ID_BOW_INFINITY, 1), setCount(Items.GOLD, 3), bowW)
        bows[addEnchantment(addEnchantment(ItemBow(), Enchantment.ID_BOW_INFINITY, 1), Enchantment.ID_BOW_POWER, 1).setCustomName("Bow lvl II")] = ShopWindow(addEnchantment(addEnchantment(ItemBow(), Enchantment.ID_BOW_INFINITY, 1), Enchantment.ID_BOW_POWER, 1).setCustomName("Bow lvl II"), setCount(Items.GOLD, 7), bowW)
        bows[addEnchantment(addEnchantment(addEnchantment(ItemBow(), Enchantment.ID_BOW_INFINITY, 1), Enchantment.ID_BOW_POWER, 1), Enchantment.ID_BOW_KNOCKBACK, 1).setCustomName("Bow lvl III")] = ShopWindow(addEnchantment(addEnchantment(addEnchantment(ItemBow(), Enchantment.ID_BOW_INFINITY, 1), Enchantment.ID_BOW_POWER, 1), Enchantment.ID_BOW_KNOCKBACK, 1).setCustomName("Bow lvl III"), setCount(Items.GOLD, 13), bowW)
        bows[addEnchantment(ItemBow(), Enchantment.ID_BOW_INFINITY, 1).setCustomName("Explosive Bow")] = ShopWindow(addEnchantment(ItemBow(), Enchantment.ID_BOW_INFINITY, 1).setCustomName("Explosive Bow"), setCount(Items.GOLD, 20), bowW)
        bows[ItemArrow()] = ShopWindow(ItemArrow(), Items.GOLD.clone(), bowW)

        val food = LinkedHashMap<Item, Window>()
        food[ItemApple()] = ShopWindow(ItemApple(), Items.BRONZE.clone(), foodW)
        food[ItemPorkchopCooked()] = ShopWindow(ItemPorkchopCooked(), setCount(Items.BRONZE, 2), foodW)
        food[ItemCake()] = ShopWindow(ItemCake(), Items.IRON.clone(), foodW)
        food[ItemAppleGold()] = ShopWindow(ItemAppleGold(), setCount(Items.GOLD, 3), foodW)

        val chests = LinkedHashMap<Item, Window>()
        chests[Item.get(Item.CHEST)] = ShopWindow(Item.get(Item.CHEST), Items.IRON.clone(), chestW)
        chests[Item.get(Item.ENDER_CHEST)] = ShopWindow(Item.get(Item.ENDER_CHEST), Items.GOLD.clone(), chestW)

        val potions = LinkedHashMap<Item, Window>()
        potions[ItemPotion(ItemPotion.INSTANT_HEALTH)] = ShopWindow(ItemPotion(ItemPotion.INSTANT_HEALTH), setCount(Items.IRON, 3), potionW)
        potions[ItemPotion(ItemPotion.INSTANT_HEALTH_II)] = ShopWindow(ItemPotion(ItemPotion.INSTANT_HEALTH_II), setCount(Items.IRON, 5), potionW)
        potions[ItemPotion(ItemPotion.SPEED_LONG)] = ShopWindow(ItemPotion(ItemPotion.SPEED_LONG), setCount(Items.IRON, 7), potionW)
        potions[ItemPotion(ItemPotion.STRENGTH_LONG)] = ShopWindow(ItemPotion(ItemPotion.STRENGTH_LONG), setCount(Items.GOLD, 8), potionW)

        val specials = LinkedHashMap<Item, Window>()
        specials[Item.get(Item.SPONGE).setCustomName(TextFormat.GOLD.toString() + "Lucky Block")] = ShopWindow(Item.get(Item.SPONGE).setCustomName(TextFormat.GOLD.toString() + "Lucky Block"), setCount(Items.IRON, 5), specialW)
        specials[Item.get(Item.ENDER_PEARL)] = ShopWindow(Item.get(Item.ENDER_PEARL), setCount(Items.GOLD, 13), specialW)
        specials[Item.get(Item.SPAWN_EGG, TNTShip.NETWORK_ID).setCustomName("${TextFormat.RED}Sheepy ${TextFormat.GOLD}Sheep ${TextFormat.YELLOW}Sheep")] = ShopWindow(Item.get(Item.SPAWN_EGG, TNTShip.NETWORK_ID).setCustomName("${TextFormat.RED}Sheepy ${TextFormat.GOLD}Sheep ${TextFormat.YELLOW}Sheep"), setCount(Items.BRONZE, 64), specialW)
        specials[Item.get(Item.STONE_PRESSURE_PLATE).setCustomName(TextFormat.GOLD.toString() + "Mine")] = ShopWindow(Item.get(Item.STONE_PRESSURE_PLATE).setCustomName(TextFormat.GOLD.toString() + "Mine"), setCount(Items.IRON, 5), specialW)
        //TODO: PRUT
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

    private fun setCount(i: Item, count: Int): Item {
        val item = i.clone()
        item.setCount(count)
        return item
    }

    private fun addEnchantment(item: Item, id: Int, lvl: Int): Item {
        val e = Enchantment.get(id)
        e.setLevel(lvl, false)
        item.addEnchantment(e)

        return item
    }
}
