package com.creeperface.nukkit.bedwars.api.event

import cn.nukkit.event.Event
import com.creeperface.nukkit.bedwars.api.BedWarsAPI
import com.creeperface.nukkit.bedwars.api.arena.Arena

open class ArenaEvent(
        val api: BedWarsAPI,
        val arena: Arena
) : Event()