package com.creeperface.nukkit.bedwars.economy

import com.creeperface.nukkit.bedwars.api.economy.EconomyProvider
import java.util.concurrent.CompletableFuture

object NoneEconomyProvider : EconomyProvider {

    override val defaultCurrency = EconomyProvider.NullCurrency

    override fun addMoney(player: String, amount: Int, currency: EconomyProvider.Currency) {

    }

    override fun getMoney(player: String, currency: EconomyProvider.Currency): CompletableFuture<Int> {
        return CompletableFuture.completedFuture(0)
    }

    override fun transferMoney(from: String, to: String, amount: Int, currency: EconomyProvider.Currency): CompletableFuture<Boolean> {
        return CompletableFuture.completedFuture(true)
    }
}