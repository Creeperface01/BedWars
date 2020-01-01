package com.creeperface.nukkit.bedwars.shop.form

import cn.nukkit.Player
import cn.nukkit.form.element.ElementButton
import cn.nukkit.form.element.ElementButtonImageData
import cn.nukkit.form.response.FormResponseSimple
import cn.nukkit.form.window.FormWindow
import cn.nukkit.form.window.FormWindowSimple
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.shop.ShopMenuWindow
import com.creeperface.nukkit.bedwars.api.shop.ShopOfferWindow
import com.creeperface.nukkit.bedwars.api.shop.ShopWindow
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.obj.BedWarsData
import com.creeperface.nukkit.bedwars.utils.TF
import com.creeperface.nukkit.bedwars.utils.plus

/**
 * @author CreeperFace
 */
class FormShopManager(private val plugin: BedWars) {

    fun addWindow(p: Player, window: ShopWindow) {
        when (window) {
            is ShopMenuWindow -> addWindow(p, window)
            is ShopOfferWindow -> addWindow(p, window)
        }
    }

    fun addWindow(p: Player, shopWindow: ShopMenuWindow) {
        val window = FormWindowSimple(shopWindow.windowName, "")

        shopWindow.windows.values.forEach {
            window.addButton(ElementButton(
                    it.windowName,
                    ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_PATH, it.icon.itemPath)
            ))
        }

        p.showFormWindow(window, shopWindow)
    }

    fun addWindow(p: Player, shopWindow: ShopOfferWindow) {
        var content =
                TF.GRAY + Lang.SHOP_ITEM.translate() + ": " + TF.YELLOW + shopWindow.item.name + "\n"
        content += TF.GRAY + Lang.SHOP_COST.translate() + ": " + "\n"

        shopWindow.cost.forEach { cost ->
            content += TF.GRAY + "  - " + TF.YELLOW + cost.name
        }

        val window = FormWindowSimple(shopWindow.windowName, content)
        p.showFormWindow(window, shopWindow)
    }

    private fun Player.showFormWindow(form: FormWindow, window: ShopWindow) {
        this.showFormWindow(form, MAIN_ID_START_BIT or window.id)
    }

    fun handleResponse(p: Player, data: BedWarsData, id: Int, response: FormResponseSimple) {
        val team = data.team
        val window = team.windowMap[id] ?: return

        if (window is ShopMenuWindow) {
            val clickedWindow = window[response.clickedButtonId] ?: return
            addWindow(p, clickedWindow)
        } else if (window is ShopOfferWindow) {
            when (response.clickedButtonId) {
                0 -> {
                    plugin.shop.processTransaction(p, window.item, window.cost)
                }
                else -> {
                    window.parent?.let { parent ->
                        addWindow(p, parent)
                    }
                }
            }
        }
    }

    companion object {

        const val MAIN_ID_START_BIT = 0b110101
    }
}
