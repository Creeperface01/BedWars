package com.creeperface.nukkit.bedwars.api

import cn.nukkit.Player
import com.creeperface.nukkit.bedwars.api.arena.Arena
import com.creeperface.nukkit.bedwars.api.data.provider.DataProvider
import com.creeperface.nukkit.bedwars.api.economy.EconomyProvider
import kotlin.reflect.KClass

interface BedWarsAPI {

    val economyProvider: EconomyProvider

    val dataProvider: DataProvider

    fun getPlayerArena(p: Player): Arena?

    fun getArena(arena: String): Arena?

    fun joinRandomArena(p: Player)

    fun getFreeArena(p: Player): Arena?

    fun registerEconomyProvider(name: String, provider: KClass<out EconomyProvider>)

    fun registerDataProvider(name: String, provider: KClass<out DataProvider>)

    companion object {

        lateinit var instance: BedWarsAPI
            private set
    }
}