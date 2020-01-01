package com.creeperface.nukkit.bedwars.api.shop

import cn.nukkit.Player

interface Shop {

    val type: ShopType

    fun open(player: Player, window: ShopWindow, type: ShopType = this.type)

//    fun createMenuWindow(icon: ShopWindow.WindowIcon, vararg windows: ShopWindow) =
//            createMenuWindow(icon, windows.toList())
//
//    fun createMenuWindow(icon: ShopWindow.WindowIcon, windows: List<ShopWindow>): ShopMenuWindow
//
//    fun createOfferWindow(icon: ShopWindow.WindowIcon, item: Item, vararg cost: Item) =
//            createOfferWindow(icon, item, cost.toList())
//
//    fun createOfferWindow(icon: ShopWindow.WindowIcon, item: Item, cost: Collection<Item>): ShopMenuWindow

}