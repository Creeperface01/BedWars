package com.creeperface.nukkit.bedwars.arena

import cn.nukkit.Player
import cn.nukkit.inventory.CustomInventory
import cn.nukkit.inventory.InventoryType
import cn.nukkit.item.Item

class VirtualInventory(p: Player) : CustomInventory(p, InventoryType.PLAYER) {

    var armor: Array<Item>

    init {
        val inv = p.inventory
        this.contents = inv.contents
        this.armor = inv.armorContents
    }

}