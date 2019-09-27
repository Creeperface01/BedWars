package bedwars.shop

import cn.nukkit.block.BlockWool
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.utils.TextFormat

class ShopWindow(item: Item, cost: Item, private val previousWindow: ItemWindow) : Window() {

    val item: Item
        get() = getItem(0)

    val cost: Item
        get() = getItem(3)

    init {
        setItem(0, item)
        setItem(3, cost)
        setItem(getSize() - 1, ItemBlock(BlockWool(), 14).setCustomName(TextFormat.AQUA.toString() + "Back"))
    }

    override fun getWindow(slot: Int): Window? {
        return if (slot == getSize() - 1) {
            previousWindow
        } else null

    }
}
