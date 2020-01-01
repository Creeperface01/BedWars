package com.creeperface.nukkit.bedwars.shop.generic

import cn.nukkit.item.Item
import com.creeperface.nukkit.bedwars.api.shop.ShopMenuWindow
import com.creeperface.nukkit.bedwars.api.shop.ShopOfferWindow
import com.creeperface.nukkit.bedwars.api.shop.ShopWindow
import com.creeperface.nukkit.bedwars.shop.inventory.OfferWindow

class GenericOfferWindow(
        id: Int,
        override var item: Item,
        override var cost: Collection<Item>,
        name: String,
        icon: ShopWindow.WindowIcon,
        parent: ShopMenuWindow?
) : ShopOfferWindow, GenericWindow<OfferWindow>(id, name, icon, parent) {

    override val inventory: OfferWindow
        get() = OfferWindow(this)
}