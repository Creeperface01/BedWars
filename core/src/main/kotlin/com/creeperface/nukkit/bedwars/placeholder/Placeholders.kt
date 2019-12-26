package com.creeperface.nukkit.bedwars.placeholder

import com.creeperface.nukkit.bedwars.BedWars

object Placeholders {

    fun init(plugin: BedWars) {
        plugin.server.pluginManager.getPlugin("PlaceholderAPI") ?: return

        //TODO: placeholders
    }
}