package bedWars.shop

abstract class Window : ShopInventory() {

    abstract fun getWindow(slot: Int): Window?
}
