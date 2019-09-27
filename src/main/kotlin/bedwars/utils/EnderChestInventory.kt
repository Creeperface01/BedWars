package bedwars.utils

import bedwars.shop.ShopInventory
import cn.nukkit.inventory.InventoryType
import org.joor.Reflect

/**
 * Created by CreeperFace on 8.5.2017.
 */
class EnderChestInventory : ShopInventory() {
    init {
        Reflect.on(this).set("type", InventoryType.ENDER_CHEST)
    }
}
