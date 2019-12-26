package com.creeperface.nukkit.bedwars.api.shop

import cn.nukkit.item.Item

interface ShopWindow {

    val parent: ShopMenuWindow?
    val icon: WindowIcon

    class WindowIcon(val item: Item, val name: String)
}