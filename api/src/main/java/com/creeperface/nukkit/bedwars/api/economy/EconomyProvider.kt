package com.creeperface.nukkit.bedwars.api.economy

import cn.nukkit.Player
import java.util.concurrent.CompletableFuture

interface EconomyProvider {

    val defaultCurrency: Currency

    fun subtractMoney(player: Player, amount: Int, currency: Currency = defaultCurrency) = addMoney(player.name, -amount, currency)

    fun subtractMoney(player: String, amount: Int, currency: Currency = defaultCurrency) = addMoney(player, -amount, currency)

    fun addMoney(player: Player, amount: Int, currency: Currency = defaultCurrency) = addMoney(player.name, amount, currency)

    fun addMoney(player: String, amount: Int, currency: Currency = defaultCurrency)

    fun getMoney(player: Player, currency: Currency = defaultCurrency) = getMoney(player.name, currency)

    fun getMoney(player: String, currency: Currency = defaultCurrency): CompletableFuture<Int>

    fun transferMoney(from: String, to: String, amount: Int, currency: Currency = defaultCurrency): CompletableFuture<Boolean>

    interface Currency
}