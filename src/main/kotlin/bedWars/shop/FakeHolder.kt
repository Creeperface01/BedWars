package bedWars.shop

import cn.nukkit.inventory.InventoryHolder

class FakeHolder : InventoryHolder {

    override fun getInventory(): ShopInventory? {
        return null
    }
}
