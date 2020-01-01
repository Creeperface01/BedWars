package com.creeperface.nukkit.bedwars.api.shop

import cn.nukkit.item.Item

interface ShopWindow {

    val id: Int

    val parent: ShopMenuWindow?
    val windowName: String
    val icon: WindowIcon
    val type: WindowType

    enum class WindowType {
        MENU,
        OFFER
    }

    class WindowIcon(val item: Item, val itemPath: String) {

        companion object {

            val EMPTY = WindowIcon(Item.get(0), "")
        }
    }
}