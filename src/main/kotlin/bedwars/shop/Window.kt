package bedwars.shop

abstract class Window : ShopInventory() {

    abstract fun getWindow(slot: Int): Window?
}
