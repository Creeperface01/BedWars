package com.creeperface.nukkit.bedwars.api.event

import cn.nukkit.event.Event
import cn.nukkit.event.HandlerList
import com.creeperface.nukkit.bedwars.api.BedWarsAPI
import com.creeperface.nukkit.bedwars.api.arena.Arena

class ArenaStartEvent(
        val api: BedWarsAPI,
        val arena: Arena
) : Event() {

    companion object {
        @JvmStatic
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlers(): HandlerList {
            return handlers
        }
    }
}