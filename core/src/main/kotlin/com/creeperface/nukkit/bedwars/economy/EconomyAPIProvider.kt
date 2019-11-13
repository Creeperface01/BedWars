package com.creeperface.nukkit.bedwars.economy

import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.economy.EconomyProvider
import me.onebone.economyapi.EconomyAPI
import java.util.concurrent.CompletableFuture

class EconomyAPIProvider(plugin: BedWars) : EconomyProvider {

    override val defaultCurrency = EconomyProvider.NullCurrency

    val api: EconomyAPI = EconomyAPI.getInstance()

    override fun addMoney(player: String, amount: Int, currency: EconomyProvider.Currency) {
        api.addMoney(player, amount.toDouble())
    }

    override fun getMoney(player: String, currency: EconomyProvider.Currency): CompletableFuture<Int> {
        return CompletableFuture.completedFuture(api.myMoney(player).toInt())
    }

    override fun transferMoney(from: String, to: String, amount: Int, currency: EconomyProvider.Currency): CompletableFuture<Boolean> {
        return CompletableFuture.completedFuture(
                api.reduceMoney(from, amount.toDouble()) == 1 &&
                        api.addMoney(to, amount.toDouble()) == 1
        )
    }
}