package com.creeperface.nukkit.bedwars.economy

import com.creeperface.nukkit.bedwars.api.economy.EconomyProvider
import java.util.concurrent.CompletableFuture

object NoneEconomyProvider : EconomyProvider {

    override val defaultCurrency: EconomyProvider.Currency
        get() = throw UnsupportedOperationException()

    override fun addMoney(player: String, amount: Int, currency: EconomyProvider.Currency) {
        throw UnsupportedOperationException()
    }

    override fun getMoney(player: String, currency: EconomyProvider.Currency): CompletableFuture<Int> {
        throw UnsupportedOperationException()
    }

    override fun transferMoney(from: String, to: String, amount: Int, currency: EconomyProvider.Currency): CompletableFuture<Boolean> {
        throw UnsupportedOperationException()
    }
}