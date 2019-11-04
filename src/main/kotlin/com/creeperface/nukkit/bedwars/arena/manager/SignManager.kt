package com.creeperface.nukkit.bedwars.arena.manager

import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.utils.plus

class SignManager(private val arena: Arena) {

    val teamSigns = ArrayList<Array<String>>(arena.teams.size)
    var lastTeamSignsUpdate = 0L
        private set

    val mainSign = Array(4) { "" }
    var lastMainSignUpdate = 0L
        private set

    internal fun init() {
        for (i in 0 until arena.teams.size) {
            teamSigns[i] = Array(4) { "" }
        }

        updateMainSign()
        updateTeamSigns()
    }

    internal fun updateMainSign() {
        val mapname = arena.map
        val map: String

        map = when {
            arena.game == Arena.ArenaState.LOBBY -> if (arena.multiPlatform) "---" else "" + TextFormat.BOLD + TextFormat.LIGHT_PURPLE + "PE ONLY"
            else -> mapname
        }

        var game = TextFormat.GREEN + "Lobby"
        if (arena.game == Arena.ArenaState.GAME) {
            game = TextFormat.RED + "Ingame"
        }
        if (arena.game != Arena.ArenaState.LOBBY && !arena.canJoin) {
            game = "§c§lRESTART"
        }

        this.mainSign[0] = TextFormat.DARK_RED + "■" + arena.name + "■"
        this.mainSign[1] = TextFormat.BLACK + arena.playerData.size + "/" + arena.maxPlayers
        this.mainSign[2] = game
        this.mainSign[3] = TextFormat.BOLD + TextFormat.BLACK + map

        lastMainSignUpdate = System.currentTimeMillis()
    }

    internal fun updateTeamSigns() {
        teamSigns.forEachIndexed { index, data ->
            val team = arena.teams[index]

            data[1] = TextFormat.BOLD + team.chatColor + team.name.toUpperCase()
            data[2] = TextFormat.GRAY + team.players.size + " players"
        }

        lastTeamSignsUpdate = System.currentTimeMillis()
    }

}