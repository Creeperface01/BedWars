package com.creeperface.nukkit.bedwars.api.arena

import cn.nukkit.Player
import com.creeperface.nukkit.bedwars.api.utils.Lang

interface Arena {

    val players: Map<String, PlayerData>
    
    val spectators: Map<String, Player>

    var gameState: ArenaState

    val aliveTeams: List<Team>

    fun joinToArena(p: Player)

    fun leaveArena(p: Player)

    fun startGame()

    fun stopGame()

    fun inArena(p: Player): Boolean

    fun getPlayerTeam(p: Player): Team?

    fun isTeamFree(team: Team): Boolean

    fun messageAllPlayers(lang: Lang, vararg args: String)

    fun messageAllPlayers(lang: Lang, addPrefix: Boolean = false, vararg args: String)

    fun isSpectator(p: Player): Boolean

    fun setSpectator(p: Player, respawn: Boolean = false)

    fun getPlayerData(p: Player): PlayerData?

    fun getTeam(id: Int): Team

    enum class ArenaState {
        LOBBY,
        GAME
    }
}