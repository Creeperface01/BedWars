package bedWars.shop

import cn.nukkit.block.BlockWool
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.utils.TextFormat

class ItemWindow @JvmOverloads constructor(main: Boolean = false) : Window() {

    private val isMain: Boolean = main

    private var previousWindow: Window? = null

    private val windows = LinkedHashMap<Int, Window>()

    @JvmOverloads
    fun setWindows(list: Map<Item, Window>, previousWindow: Window? = null) {
        var i = 0

        for ((item, win) in list) {

            setItem(i, item)
            windows[i] = win
            i++
        }

        if (!isMain) {
            val item = ItemBlock(BlockWool(), 14)
            item.customName = TextFormat.AQUA.toString() + "Back"

            setItem(getSize() - 1, item)
            windows[getSize() - 1] = previousWindow!!
        }

        this.previousWindow = previousWindow
    }

    override fun getWindow(slot: Int): Window? {
        return windows[slot]
    }
}
