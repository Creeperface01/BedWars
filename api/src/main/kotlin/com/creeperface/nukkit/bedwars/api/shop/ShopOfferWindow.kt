package com.creeperface.nukkit.bedwars.api.shop

import cn.nukkit.item.Item

interface ShopOfferWindow : ShopWindow {

    val item: Item
    val cost: Collection<Item>

    override val type: ShopWindow.WindowType
        get() = ShopWindow.WindowType.OFFER

}