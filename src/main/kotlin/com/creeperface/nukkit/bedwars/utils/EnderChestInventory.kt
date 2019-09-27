package com.creeperface.nukkit.bedwars.utils

import cn.nukkit.inventory.InventoryType
import com.creeperface.nukkit.bedwars.shop.ShopInventory
import org.joor.Reflect

/**
 * Created by CreeperFace on 8.5.2017.
 */
class EnderChestInventory : ShopInventory() {
    init {
        Reflect.on(this).set("type", InventoryType.ENDER_CHEST)
    }
}
