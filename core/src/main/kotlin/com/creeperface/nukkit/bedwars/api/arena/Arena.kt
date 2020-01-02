package com.creeperface.nukkit.bedwars.api.arena

import cn.nukkit.Player
import cn.nukkit.level.Level
import com.creeperface.nukkit.bedwars.api.arena.configuration.IArenaConfiguration
import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.utils.Lang

interface Arena : IArenaConfiguration {

    val players: Map<String, PlayerData>
    val spectators: Map<String, Player>

    var gameState: ArenaState

    val teams: List<Team>
    val aliveTeams: List<Team>

    val mapConfig: MapConfiguration
    val level: Level

    val starting: Boolean
    val ending: Boolean

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