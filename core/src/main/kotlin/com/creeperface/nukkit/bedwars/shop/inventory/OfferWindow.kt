package com.creeperface.nukkit.bedwars.shop.inventory

import cn.nukkit.block.BlockWool
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.api.shop.ShopOfferWindow
import com.creeperface.nukkit.bedwars.api.shop.ShopWindow

class OfferWindow(override val item: Item, override val cost: Collection<Item>, parent: MenuWindow, icon: ShopWindow.WindowIcon) : Window(parent, icon), ShopOfferWindow {

    init {
        if (cost.size > this.size - 5) {
            throw RuntimeException("Cost items count exceeded allowed size (${this.size - 5})")
        }

        setItem(0, item)

        cost.forEachIndexed { index, costItem ->
            setItem(3 + index, costItem)
        }

        setItem(getSize() - 1, ItemBlock(BlockWool(), 14).setCustomName(TextFormat.AQUA.toString() + "Back"))
    }

    override fun getWindow(slot: Int): Window? {
        return if (slot == getSize() - 1) {
            parent
        } else null
    }
}
