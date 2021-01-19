package com.creeperface.nukkit.bedwars.shop.inventory

import cn.nukkit.block.BlockWool
import cn.nukkit.item.ItemBlock
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.api.shop.ShopWindow

abstract class Window(window: ShopWindow) : ShopInventory(), ShopWindow {

    override val id = window.id
    override val parent: MenuWindow?

    override val windowName = window.windowName
    override val icon = window.icon

    init {
        window.parent.let {
            parent = if (it is MenuWindow) {
                // non-main window
                val item = ItemBlock(BlockWool(), 14)
                item.customName = TextFormat.AQUA.toString() + "Back"

                this.setItem(getSize() - 1, item)

//                MenuWindow(it)
                it
            } else null
        }
    }
}
