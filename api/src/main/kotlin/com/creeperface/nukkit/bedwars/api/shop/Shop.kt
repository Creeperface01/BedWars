package com.creeperface.nukkit.bedwars.api.shop

import cn.nukkit.item.Item

interface Shop {

    val mainWindow: ShopWindow

    fun open(window: ShopWindow, type: ShopType)

    fun createMenuWindow(icon: ShopWindow.WindowIcon, windows: List<ShopWindow>): ShopMenuWindow

    fun createOfferWindow(icon: ShopWindow.WindowIcon, item: Item, cost: Collection<Item>): ShopMenuWindow

}