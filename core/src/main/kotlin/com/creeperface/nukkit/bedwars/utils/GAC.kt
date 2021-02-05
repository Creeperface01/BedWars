package com.creeperface.nukkit.bedwars.utils

import cn.nukkit.Player
import cn.nukkit.block.Block
import com.creeperface.nukkit.bedwars.BedWars
import cz.creeperface.nukkit.gac.checks.NukerCheck

object GAC {

    private var enabled = false

    fun init() {
        enabled = BedWars.instance.server.pluginManager.getPlugin("GAC") != null
    }

    fun checkNuker(player: Player, block: Block): Boolean {
        if (!enabled) {
            return true
        }

        return NukerCheck.a.run(player, block)
    }
}