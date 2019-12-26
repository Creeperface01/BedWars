package com.creeperface.nukkit.bedwars.shop.inventory

import cn.nukkit.block.BlockWool
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.api.shop.ShopMenuWindow
import com.creeperface.nukkit.bedwars.api.shop.ShopWindow

class MenuWindow(parent: MenuWindow?, icon: ShopWindow.WindowIcon) : Window(parent, icon), ShopMenuWindow {

    override val windows: MutableMap<Int, Window> = LinkedHashMap()

    fun setWindows(list: Map<Item, Window>) {
        var i = 0

        for ((item, win) in list) {
            setItem(i, item)
            windows[i] = win
            i++
        }

        parent?.let {
            // non-main window
            val item = ItemBlock(BlockWool(), 14)
            item.customName = TextFormat.AQUA.toString() + "Back"

            setItem(getSize() - 1, item)
            windows[getSize() - 1] = it
        }
    }

    override fun getWindow(slot: Int) = windows[slot]
}
