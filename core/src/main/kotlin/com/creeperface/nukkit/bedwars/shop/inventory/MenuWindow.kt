package com.creeperface.nukkit.bedwars.shop.inventory

import cn.nukkit.item.Item
import com.creeperface.nukkit.bedwars.api.shop.ShopMenuWindow
import com.creeperface.nukkit.bedwars.utils.toInventory

class MenuWindow(window: ShopMenuWindow) : Window(window), ShopMenuWindow {

    override val windows = window.windows.mapValues { it.value.toInventory() }.toMutableMap()

    override fun get(index: Int) = windows[index]

    fun setWindows(list: Map<Item, Window>) {
        var i = 0

        for ((item, win) in list) {
            setItem(i, item)
            windows[i] = win
            i++
        }
    }
}
