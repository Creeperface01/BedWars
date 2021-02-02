package com.creeperface.nukkit.bedwars.utils

import cn.nukkit.inventory.InventoryType
import com.creeperface.nukkit.bedwars.shop.inventory.ShopInventory

/**
 * Created by CreeperFace on 8.5.2017.
 */
class EnderChestInventory : ShopInventory() {
    init {
        this.setProperty("type", InventoryType.ENDER_CHEST)
//        Reflect.on(this).set("type", InventoryType.ENDER_CHEST)
    }
}
