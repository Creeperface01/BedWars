package com.creeperface.nukkit.bedwars.economy

import com.creeperface.nukkit.bedwars.api.economy.EconomyProvider
import java.util.concurrent.CompletableFuture

object NoneEconomyProvider : EconomyProvider {

    override val defaultCurrency = EconomyProvider.NullCurrency

    override fun addMoney(player: String, amount: Double, currency: EconomyProvider.Currency) {

    }

    override fun getMoney(player: String, currency: EconomyProvider.Currency): CompletableFuture<Double> {
        return CompletableFuture.completedFuture(0.0)
    }

    override fun transferMoney(from: String, to: String, amount: Double, currency: EconomyProvider.Currency): CompletableFuture<Boolean> {
        return CompletableFuture.completedFuture(true)
    }

    override fun getCurrency(name: String): EconomyProvider.Currency? = null
}