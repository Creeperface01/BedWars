package com.creeperface.nukkit.bedwars.shop.formShop

import cn.nukkit.Player
import cn.nukkit.form.element.ElementButton
import cn.nukkit.form.window.FormWindowSimple
import cn.nukkit.item.Item
import com.creeperface.nukkit.bedwars.obj.Team

/**
 * @author CreeperFace
 */
class FormShopManager {

    companion object {

        const val MAIN_ID = 472095000

        const val BLOCKS_ID = 472095001
        const val ARMOR_ID = 472095002
        const val PICKAXE_ID = 472095003
        const val SWORD_ID = 472095004
        const val BOW_ID = 472095005
        const val FOOD_ID = 472095006
        const val CHEST_ID = 472095007
        const val POTION_ID = 472095008
        const val SPECIAL_ID = 472095009

        private val entries = mutableMapOf<Int, Array<ShopEntry>>()

        /*init {
            entries[BLOCKS_ID] = arrayOf(ShopEntry("shop_blocks", ButtonEntry("")))
        }*/
    }

    fun addMainWindow(p: Player) {
        val window = FormWindowSimple("Shop", "")
        window.addButton(ElementButton("Blocks"))
        window.addButton(ElementButton("Armor"))
        window.addButton(ElementButton("Pickaxes"))
        window.addButton(ElementButton("Swords"))
        window.addButton(ElementButton("Bows"))
        window.addButton(ElementButton("Food"))
        window.addButton(ElementButton("Chests"))
        window.addButton(ElementButton("Potions"))
        window.addButton(ElementButton("Special"))

        p.showFormWindow(window, MAIN_ID)
    }

    private data class ShopEntry(val title: String, val buttonData: ButtonEntry)

    private data class ButtonEntry(val title: String, val image: String, val item: Item?, val loader: ((Team) -> Item)?)
}
