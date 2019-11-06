package com.creeperface.nukkit.bedwars.placeholder

import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI

object Placeholders {

    fun init(plugin: BedWars) {
        val api = plugin.server.pluginManager.getPlugin("PlaceholderAPI") ?: return

        api as PlaceholderAPI

        //TODO: placeholders
    }
}