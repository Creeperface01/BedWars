package com.creeperface.nukkit.bedwars.shop.generic

import com.creeperface.nukkit.bedwars.api.shop.ShopMenuWindow
import com.creeperface.nukkit.bedwars.api.shop.ShopWindow
import com.creeperface.nukkit.bedwars.shop.inventory.MenuWindow

class GenericMenuWindow(
        id: Int,
        windows: MutableMap<Int, ShopWindow>,
        name: String,
        icon: ShopWindow.WindowIcon,
        parent: ShopMenuWindow? = null
) : ShopMenuWindow, GenericWindow<MenuWindow>(id, name, icon, parent) {

    override val inventory: MenuWindow
        get() = MenuWindow(this)

    override val windows = windows
        get() = field.toMutableMap()

    override fun get(index: Int) = this.windows[index]

    companion object {

        fun create(
                id: Int,
                name: String,
                windows: MutableMap<Int, ShopWindow> = mutableMapOf(),
                icon: ShopWindow.WindowIcon = ShopWindow.WindowIcon.EMPTY
        ) = GenericMenuWindow(id, windows, name, icon)
    }
}