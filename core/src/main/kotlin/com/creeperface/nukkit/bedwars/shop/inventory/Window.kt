package com.creeperface.nukkit.bedwars.shop.inventory

import com.creeperface.nukkit.bedwars.api.shop.ShopWindow

abstract class Window(override val parent: MenuWindow?, override val icon: ShopWindow.WindowIcon) : ShopInventory(), ShopWindow {

    abstract fun getWindow(slot: Int): Window?
}
