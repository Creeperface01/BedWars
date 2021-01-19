package com.creeperface.nukkit.bedwars.shop.inventory

import com.creeperface.nukkit.bedwars.api.shop.ShopMenuWindow
import com.creeperface.nukkit.bedwars.api.shop.ShopWindow
import com.creeperface.nukkit.bedwars.utils.toInventory

class MenuWindow(window: ShopMenuWindow) : Window(window), ShopMenuWindow {

    override val windows = mutableMapOf<Int, Window>()

    override fun get(index: Int) = windows[index]

    init {
        setWindows(window.windows)
    }

    fun setWindows(windows: Map<Int, ShopWindow>) {
        windows.forEach { (index, win) ->
            setItem(index, win.icon.item)

            this.windows[index] = win.toInventory()
        }
    }
}
