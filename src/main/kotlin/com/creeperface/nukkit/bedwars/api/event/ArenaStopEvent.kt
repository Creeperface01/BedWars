package com.creeperface.nukkit.bedwars.api.event

import cn.nukkit.event.HandlerList
import com.creeperface.nukkit.bedwars.api.BedWarsAPI
import com.creeperface.nukkit.bedwars.api.arena.Arena
import com.creeperface.nukkit.bedwars.api.arena.Team

class ArenaStopEvent(
        api: BedWarsAPI,
        arena: Arena,
        val winner: Team?,
        val cause: Cause
) : ArenaEvent(api, arena) {

    companion object {
        @JvmStatic
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlers(): HandlerList {
            return handlers
        }
    }

    enum class Cause {
        ELIMINATION,
        NO_PLAYERS,
        TIME_LIMIT,
        SHUTDOWN,
        COMMAND,
        CUSTOM
    }
}