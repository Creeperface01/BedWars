package com.creeperface.nukkit.bedwars.shop.inventory

import cn.nukkit.inventory.InventoryHolder

class FakeHolder : InventoryHolder {

    override fun getInventory(): ShopInventory? {
        return null
    }
}
