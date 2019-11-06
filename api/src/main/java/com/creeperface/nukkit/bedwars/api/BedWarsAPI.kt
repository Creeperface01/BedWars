package com.creeperface.nukkit.bedwars.api

import cn.nukkit.Player
import com.creeperface.nukkit.bedwars.api.arena.Arena

interface BedWarsAPI {

    fun getPlayerArena(p: Player): Arena?

    fun getArena(arena: String): Arena?

    fun joinRandomArena(p: Player)

    fun getFreeArena(p: Player): Arena?

    companion object {

        lateinit var instance: BedWarsAPI
            private set
    }
}