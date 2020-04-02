package com.creeperface.nukkit.bedwars.economy

import com.creeperface.nukkit.bedwars.api.economy.EconomyProvider
import com.creeperface.nukkit.bedwars.utils.Configuration
import me.onebone.economyapi.EconomyAPI
import java.util.concurrent.CompletableFuture

internal class EconomyAPIProvider(private val configuration: Configuration) : EconomyProvider {

    override val defaultCurrency = EconomyProvider.NullCurrency

    val api: EconomyAPI = EconomyAPI.getInstance()

    override fun addMoney(player: String, amount: Double, currency: EconomyProvider.Currency) {
        api.addMoney(player, amount)
    }

    override fun getMoney(player: String, currency: EconomyProvider.Currency): CompletableFuture<Double> {
        return CompletableFuture.completedFuture(api.myMoney(player))
    }

    override fun transferMoney(from: String, to: String, amount: Double, currency: EconomyProvider.Currency): CompletableFuture<Boolean> {
        return CompletableFuture.completedFuture(
                api.reduceMoney(from, amount) == 1 &&
                        api.addMoney(to, amount) == 1
        )
    }

    override fun getCurrency(name: String): EconomyProvider.Currency? = null
}