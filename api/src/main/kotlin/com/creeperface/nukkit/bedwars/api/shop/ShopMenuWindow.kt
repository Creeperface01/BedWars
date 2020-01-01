package com.creeperface.nukkit.bedwars.api.shop

interface ShopMenuWindow : ShopWindow {

    val windows: Map<Int, ShopWindow>

    override val type: ShopWindow.WindowType
        get() = ShopWindow.WindowType.MENU

//    operator fun set(index: Int, window: ShopWindow)

    operator fun get(index: Int): ShopWindow?
}