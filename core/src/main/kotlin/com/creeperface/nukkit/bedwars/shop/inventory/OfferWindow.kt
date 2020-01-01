package com.creeperface.nukkit.bedwars.shop.inventory

import com.creeperface.nukkit.bedwars.api.shop.ShopOfferWindow

class OfferWindow(window: ShopOfferWindow) : Window(window), ShopOfferWindow {

    override var item = window.item
        set(value) {
            field = value

            setItem(ITEM_SLOT, value)
        }

    override var cost = window.cost
        set(value) {
            field = value

            if (value.size > this.size - 5) {
                throw RuntimeException("Cost items count exceeded allowed size (${this.size - 5})")
            }

            value.forEachIndexed { index, costItem ->
                setItem(COST_SLOT + index, costItem)
            }
        }

    init {
        this.cost = window.cost //TODO: test calls
        this.item = window.item
    }

    companion object {

        const val ITEM_SLOT = 0
        const val COST_SLOT = 3
    }
}
