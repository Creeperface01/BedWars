package com.creeperface.nukkit.bedwars.api.event

import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import com.creeperface.nukkit.bedwars.api.BedWarsAPI
import com.creeperface.nukkit.bedwars.api.arena.Arena
import com.creeperface.nukkit.bedwars.api.arena.PlayerData
import com.creeperface.nukkit.bedwars.api.arena.Team

class ArenaBedDestroyEvent(
        api: BedWarsAPI,
        arena: Arena,
        val playerData: PlayerData,
        val team: Team
) : ArenaEvent(api, arena), Cancellable {

    companion object {
        @JvmStatic
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlers(): HandlerList {
            return handlers
        }
    }
}