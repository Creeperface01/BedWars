package bedWars.utils


import bedWars.entity.TNTShip
import cn.nukkit.block.BlockAir
import cn.nukkit.inventory.Inventory
import cn.nukkit.item.*
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.utils.TextFormat
import java.util.*

object Items {

    val BRONZE = Item.get(Item.BRICK).setCustomName("§r§6Bronz")
    val IRON = Item.get(Item.IRON_INGOT).setCustomName("§r§7Iron")
    val GOLD = Item.get(Item.GOLD_INGOT).setCustomName("§r§eGold")

    val CHESTPLATE_I = addEnchantment(addEnchantment(ItemChestplateChain(), Enchantment.ID_DURABILITY, 1), Enchantment.ID_PROTECTION_ALL, 1).setCustomName("Chestplate lvl I")
    val CHESTPLATE_II = addEnchantment(addEnchantment(ItemChestplateChain(), Enchantment.ID_DURABILITY, 1), Enchantment.ID_PROTECTION_ALL, 2).setCustomName("Chestplate lvl I")
    val CHESTPLATE_III = addEnchantment(addEnchantment(ItemChestplateChain(), Enchantment.ID_DURABILITY, 1), Enchantment.ID_PROTECTION_ALL, 3).setCustomName("Chestplate lvl I")

    val SWORD_I = addEnchantment(addEnchantment(ItemSwordGold(), Enchantment.ID_DURABILITY, 1), Enchantment.ID_DAMAGE_ALL, 1).setCustomName("Sword lvl I")
    val SWORD_II = addEnchantment(addEnchantment(ItemSwordGold(), Enchantment.ID_DURABILITY, 1), Enchantment.ID_DAMAGE_ALL, 2).setCustomName("Sword lvl I")
    val SWORD_III = addEnchantment(addEnchantment(addEnchantment(ItemSwordIron(), Enchantment.ID_KNOCKBACK, 1), Enchantment.ID_DAMAGE_ALL, 2).setCustomName("Sword lvl III"), Enchantment.ID_DURABILITY, 2)

    val PICK_I = addEnchantment(addEnchantment(ItemPickaxeWood(), Enchantment.ID_EFFICIENCY, 1), Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl I")
    val PICK_II = addEnchantment(addEnchantment(ItemPickaxeStone(), Enchantment.ID_EFFICIENCY, 1), Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl II")
    val PICK_III = addEnchantment(addEnchantment(ItemPickaxeIron(), Enchantment.ID_EFFICIENCY, 3), Enchantment.ID_DURABILITY, 1).setCustomName("Pickaxe lvl III")

    val BOW_I = addEnchantment(ItemBow(), Enchantment.ID_BOW_INFINITY, 1).setCustomName("Bow lvl I")
    val BOW_II = addEnchantment(addEnchantment(ItemBow(), Enchantment.ID_BOW_INFINITY, 1), Enchantment.ID_BOW_POWER, 1).setCustomName("Bow lvl II")
    val BOW_III = addEnchantment(addEnchantment(addEnchantment(ItemBow(), Enchantment.ID_BOW_INFINITY, 1), Enchantment.ID_BOW_POWER, 1), Enchantment.ID_BOW_KNOCKBACK, 1).setCustomName("Bow lvl III")
    val EXP_BOW = addEnchantment(ItemBow(), Enchantment.ID_BOW_INFINITY, 1).setCustomName("Explosive Bow")
    val ARROW: Item = ItemArrow()

    val CHEST = Item.get(Item.CHEST)
    val ENDER_CHEST = Item.get(Item.ENDER_CHEST)
    val LUCKY_BLOCK = Item.get(Item.SPONGE).setCustomName(TextFormat.GOLD.toString() + "Lucky Block")
    val MINE = Item.get(Item.STONE_PRESSURE_PLATE).setCustomName(TextFormat.GOLD.toString() + "Mine")
    val ENDER_PEARL = Item.get(Item.ENDER_PEARL)
    val SANDSTONE = Item.get(Item.SANDSTONE, 0, 25)
    val END_STONE = Item.get(Item.END_STONE, 0, 15)
    val HEAL: Item = ItemPotion(ItemPotion.INSTANT_HEALTH)
    val HEAL_II: Item = ItemPotion(ItemPotion.INSTANT_HEALTH_II)
    val SPEED: Item = ItemPotion(ItemPotion.SPEED_LONG)
    val STRENGTH: Item = ItemPotion(ItemPotion.STRENGTH_LONG)
    val IRON_BLOCK = Item.get(Item.IRON_BLOCK, 0, 14)
    val PORKCHOP: Item = ItemPorkchopCooked(0, 16)
    val G_APPLE: Item = ItemAppleGold()

    //legendary
    val LEGEND_SWORD = addEnchantment(addEnchantment(addEnchantment(addEnchantment(ItemSwordDiamond(), Enchantment.ID_DAMAGE_ALL, 3), Enchantment.ID_KNOCKBACK, 2), Enchantment.ID_FIRE_ASPECT, 2), Enchantment.ID_DURABILITY, 3).setCustomName(TextFormat.AQUA.toString() + "Legendary Sword")
    val LEGEND_BOW = addEnchantment(addEnchantment(addEnchantment(addEnchantment(addEnchantment(ItemBow(), Enchantment.ID_BOW_POWER, 4), Enchantment.ID_BOW_KNOCKBACK, 1), Enchantment.ID_BOW_FLAME, 1), Enchantment.ID_DURABILITY, 3), Enchantment.ID_BOW_INFINITY, 1).setCustomName(TextFormat.AQUA.toString() + "Legendary Bow")
    val LEGEND_PICKAXE = addEnchantment(addEnchantment(ItemPickaxeDiamond(), Enchantment.ID_EFFICIENCY, 5), Enchantment.ID_DURABILITY, 3).setCustomName(TextFormat.AQUA.toString() + "Legendary Pickaxe")

    val SHEEP = Item.get(Item.SPAWN_EGG, TNTShip.NETWORK_ID).setCustomName("${TextFormat.RED}Sheepy ${TextFormat.GOLD}Sheep ${TextFormat.YELLOW}Sheep")

    private val luckItems = mutableListOf<Item>()

    val luckyBlock: Item
        get() = luckItems[Random().nextInt(luckItems.size)]

    init {
        luckItems.add(CHESTPLATE_I)
        luckItems.add(CHESTPLATE_II)
        luckItems.add(CHESTPLATE_III)
        luckItems.add(SWORD_I)
        luckItems.add(SWORD_II)
        luckItems.add(SWORD_III)
        luckItems.add(PICK_I)
        luckItems.add(PICK_II)
        luckItems.add(PICK_III)
        luckItems.add(BOW_I)
        luckItems.add(BOW_II)
        luckItems.add(BOW_III)
        luckItems.add(EXP_BOW)
        luckItems.add(ARROW)
        luckItems.add(CHEST)
        luckItems.add(ENDER_CHEST)
        luckItems.add(LUCKY_BLOCK)
        luckItems.add(MINE)
        luckItems.add(ENDER_PEARL)
        luckItems.add(SANDSTONE)
        luckItems.add(END_STONE)
        luckItems.add(HEAL)
        luckItems.add(HEAL_II)
        luckItems.add(SPEED)
        luckItems.add(STRENGTH)
        luckItems.add(IRON_BLOCK)
        luckItems.add(PORKCHOP)
        luckItems.add(G_APPLE)

        luckItems.add(LEGEND_BOW)
        luckItems.add(LEGEND_PICKAXE)
        luckItems.add(LEGEND_SWORD)
        luckItems.add(SHEEP)
    }

    fun containsItem(inventory: Inventory, item: Item): Boolean {
        var count = Math.max(1, item.getCount())

        for (i in 0 until inventory.size) {
            val item2 = inventory.getItem(i)

            if (item2.equals(item, true, false) && item2.getCount() > 0) {
                count -= item2.getCount()
            }

            if (count <= 0) {
                return true
            }
        }

        return false
    }

    fun removeItem(inv: Inventory, item: Item) {
        var count = item.getCount()

        for (i in 0 until inv.size) {
            val item1 = inv.getItem(i)

            if (item1.equals(item, true, false)) {
                if (count <= item1.count) {
                    item1.count -= count
                    inv.setItem(i, item1)
                    return
                } else {
                    count -= item1.getCount()
                    inv.setItem(i, ItemBlock(BlockAir()))
                }
            }
        }
    }

    fun setCount(i: Item, count: Int): Item {
        val item = i.clone()
        item.setCount(count)
        return item
    }

    fun addEnchantment(item: Item, id: Int, lvl: Int): Item {
        val e = Enchantment.get(id)
        e.setLevel(lvl, false)
        item.addEnchantment(e)

        return item
    }
}
