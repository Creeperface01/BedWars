package com.creeperface.nukkit.bedwars.obj

import cn.nukkit.Player
import com.creeperface.nukkit.bedwars.api.data.Stats
import com.creeperface.nukkit.bedwars.arena.Arena

/**
 * Created by CreeperFace on 3.7.2017.
 */
class GlobalData(val player: Player, val stats: Stats) {

    var arena: Arena? = null
}
