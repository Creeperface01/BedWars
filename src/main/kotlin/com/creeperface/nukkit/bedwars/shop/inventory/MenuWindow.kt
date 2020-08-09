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
//        logInfo("name $windowName")
//        logInfo("windows: ${windows.size}")
//        logInfo("size: ${this.size}")
        windows.forEach { (index, win) ->
//            logInfo("setting item $index ${win.icon.item}")
            setItem(index, win.icon.item)
//            logInfo("get item: " + getItem(index))

            this.windows[index] = win.toInventory()
        }

//        logInfo("contents: $contents")
    }
}
