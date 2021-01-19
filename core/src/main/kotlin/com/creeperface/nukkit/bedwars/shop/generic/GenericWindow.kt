package com.creeperface.nukkit.bedwars.shop.generic

import com.creeperface.nukkit.bedwars.api.shop.ShopMenuWindow
import com.creeperface.nukkit.bedwars.api.shop.ShopWindow
import com.creeperface.nukkit.bedwars.shop.inventory.Window

abstract class GenericWindow<T : Window>(
    override val id: Int,
    override var windowName: String,
    override val icon: ShopWindow.WindowIcon,
    override val parent: ShopMenuWindow?
) : ShopWindow {

    abstract val inventory: T
}