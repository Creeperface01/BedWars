package com.creeperface.nukkit.bedwars.placeholder

import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.utils.ArenaContext
import com.creeperface.nukkit.bedwars.api.utils.TeamContext
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI

object Placeholders {

    fun init(plugin: BedWars) {
        plugin.server.pluginManager.getPlugin("PlaceholderAPI") ?: return

        val api = PlaceholderAPI.getInstance()

        //global placeholders
        api.buildStatic("bedwars_arenas") { _ ->
            plugin.ins
        }

        //arena placeholders
        api.buildStatic("arena_players") { _, context: ArenaContext ->
            context.context.players.values.map { it.player }
        }.build()

        api.buildStatic("arena_spectators") { _, context: ArenaContext ->
            context.context.spectators.values
        }.build()

        api.buildStatic("arena_state") { _, context: ArenaContext ->
            context.context.gameState
        }.build()

        api.buildStatic("arena_starting") { _, context: ArenaContext ->
            context.context.starting
        }.build()

        api.buildStatic("arena_ending") { _, context: ArenaContext ->
            context.context.ending
        }.build()

        //team placeholders
        api.buildStatic("team_color") { _, context: TeamContext ->
            context.context.color
        }.build()

        api.buildStatic("team_name") { _, context: TeamContext ->
            context.context.name
        }.build()

        api.buildStatic("team_bed") { _, context: TeamContext ->
            context.context.hasBed()
        }.build()

        api.buildStatic("team_players") { _, context: TeamContext ->
            context.context.getTeamPlayers().values.map { it.player.name }
        }.build()
    }
}