package com.creeperface.nukkit.bedwars.arena.manager

import com.creeperface.nukkit.bedwars.api.arena.GAME
import com.creeperface.nukkit.bedwars.api.utils.handle
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.arena.ArenaState
import com.creeperface.nukkit.bedwars.arena.handler.ArenaLobby
import com.creeperface.nukkit.bedwars.utils.TF
import com.creeperface.nukkit.bedwars.utils.plus

class SignManager(private val arena: Arena) {

//    private val teamSigns = ArrayList<Array<String>>(arena.teams.size)

    var lastTeamSignsUpdate = 0L
        private set

    val mainSign = Array(4) { "" }
    var lastMainSignUpdate = 0L
        private set

    internal fun init() {
//        for (i in arena.teams.indices) {
//            teamSigns[i] = Array(4) { "" }
//        }

        updateMainSign()
//        updateTeamSigns()
    }

//    fun getData(team: Int) = teamSigns[team]

    internal fun updateMainSign() {
        val mapName = arena.handle(ArenaState.GAME) { mapConfig.name } ?: "Voting"

        val map = arena.handle<ArenaLobby, String> {
            if (arena.multiPlatform) {
                "---"
            } else {
                "" + TF.BOLD + TF.LIGHT_PURPLE + "PE ONLY"
            }
        } ?: mapName

        val game = when (arena.state) {
            GAME -> if (arena.canJoin) TF.RED + "In-game" else TF.RED + TF.BOLD + "RESTART"
            else -> TF.GREEN + "Lobby"
        }

        this.mainSign[0] = TF.DARK_RED + "■" + arena.name + "■"
        this.mainSign[1] = TF.BLACK + arena.arenaPlayers.size + "/" + arena.maxPlayers
        this.mainSign[2] = game
        this.mainSign[3] = TF.BOLD + TF.BLACK + map

        lastMainSignUpdate = System.currentTimeMillis()
    }

//    internal fun updateTeamSigns() {
//        teamSigns.forEachIndexed { index, data ->
//            val team = arena.teams[index]
//
//            data[1] = TextFormat.BOLD + team.chatColor + team.name.toUpperCase()
//            data[2] = TextFormat.GRAY + team.players.size + " players"
//        }
//
//        lastTeamSignsUpdate = System.currentTimeMillis()
//    }

}