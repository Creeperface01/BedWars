package com.creeperface.nukkit.bedwars.api.utils

import cn.nukkit.inventory.Inventory
import cn.nukkit.item.Item

data class InventoryItem(
        val slot: Int,
        val item: Item
) {

    fun set(inventory: Inventory) {
        inventory.setItem(slot, item)
    }
}