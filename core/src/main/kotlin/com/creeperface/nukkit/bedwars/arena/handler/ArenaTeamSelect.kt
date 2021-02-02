package com.creeperface.nukkit.bedwars.arena.handler

import cn.nukkit.Player
import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.arena.handler.LobbyHandler
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.arena.ArenaState
import com.creeperface.nukkit.bedwars.arena.Team
import com.creeperface.nukkit.bedwars.obj.BedWarsData
import com.creeperface.nukkit.bedwars.utils.*
import com.creeperface.nukkit.kformapi.KFormAPI
import com.creeperface.nukkit.kformapi.form.util.showForm
import com.creeperface.nukkit.placeholderapi.api.scope.MessageScope
import com.creeperface.nukkit.placeholderapi.api.util.translatePlaceholders

class ArenaTeamSelect(
    arena: Arena,
    mapConfig: MapConfiguration? = null
) : ArenaLobby(arena), LobbyHandler.TeamSelectHandler {

    override var starting = false

    override val teams: List<Team>
    val playerData = mutableMapOf<String, BedWarsData>()

    override val mapConfig: MapConfiguration
//    val signManager = SignManager(this)

    override val state = ArenaState.TEAM_SELECT

    init {
        this.mapConfig = mapConfig ?: selectRandomMap() ?: error("No map matching arena requirements found")

        teams = this.mapConfig.teams.mapIndexed { index, conf -> Team(arena, index, conf) }.toList()

        scoreboardManager.initTeamSelect(this)
        checkLobby()
    }

    override fun checkLobby() {
        this.starting = this.arenaPlayers.size >= this.startPlayers

        if (this.starting && this.fastStart && this.arenaPlayers.size >= this.fastStartPlayers && this.task.startTime > this.fastStartTime) {
            this.task.startTime = this.fastStartTime
        }
    }

    fun isTeamFree(team: Team): Boolean {
        val minPlayers = this.teams.filter { it !== team }.minByOrNull { it.players.size }?.players?.size
            ?: team.players.size

        return team.players.size - minPlayers < 2
    }

    override fun getPlayerTeam(p: Player): Team? {
        return playerData[p.name.toLowerCase()]?.team
    }

    override fun isTeamFree(team: APITeam) = isTeamFree(team as Team)

    fun addToTeam(p: Player, team: Team) {
        var data = playerData[p.name.toLowerCase()]

        if (data == null && (isTeamFull(team) || !isTeamFree(team)) && !p.hasPermission("bedwars.joinfullteam")) {
            p.sendMessage(Lang.FULL_TEAM.translatePrefix())
            return
        }

        if (data != null && data.team === team) {
            p.sendMessage(Lang.ALREADY_IN_TEAM.translatePrefix(team.chatColor.toString() + team.name))
            return
        }

        if (data == null) {
            val globalData =
                plugin.players[p.id] ?: error("Trying to to join team when not having global player data set")

            data = BedWarsData(this.arena, p, team, globalData)
            playerData[p.name.toLowerCase()] = data
        } else {
            val currentTeam = data.team
            currentTeam.removePlayer(data)

            scoreboardManager.updateTeamPlayerCount(currentTeam)
        }

        data.team = team
        team.addPlayer(data)

//        signManager.updateTeamSigns() //TODO: sign support

        scoreboardManager.updateTeamPlayerCount(team)
        scoreboardManager.update()

        p.sendMessage(Lang.TEAM_JOIN.translate(team.chatColor.toString() + team.name))
    }

    fun Team.canPlayerJoin(p: Player): Boolean {
        return !isTeamFull(this) || isTeamFree(this) || p.hasPermission("bedwars.joinfullteam")
    }

    fun isTeamFull(team: Team): Boolean {
        return team.players.size >= 4
    }

    fun selectTeam(p: Player) {
        for (team in teams) {
            if (team.canPlayerJoin(p)) {
                val globalData =
                    plugin.players[p.id] ?: error("Trying to to join team when not having global player data set")

                val data = BedWarsData(
                    arena,
                    p,
                    team,
                    globalData
                )

                team.addPlayer(data)
                playerData[p.name.toLowerCase()] = data
                break
            }
        }
    }

    fun showTeamSelection(p: Player) {
        val form = KFormAPI.simpleForm {
            title(Lang.TEAM_SELECT.translate())

            teams.forEach { team ->
                val statusColor = if (team.canPlayerJoin(p)) TF.GREEN else TF.RED
                button(team.chatColor + team.name.ucFirst() + TF.GRAY + " - " + statusColor + team.players.size + "/" + teamPlayers) {
                    if (inArena(p)) {
                        addToTeam(p, team)
                    }
                }
            }
        }

        p.showForm(form)
    }

    fun prepareGame() {
        val levelName = mapConfig.name + "_" + this.name
        if (!this.plugin.server.isLevelLoaded(levelName)) {
            logWarning("Level $levelName hasn't been loaded")
            return
//            this.plugin.server.unloadLevel(this.plugin.server.getLevelByName(mapConfig.name))
        }

//        if (!this.plugin.server.loadLevel(mapConfig.name)) {
//            logError("Could not load arena level '${mapConfig.name}'")
//            return
//        }

        val level = this.plugin.server.getLevelByName(levelName)

        val game = ArenaGame(
            this.arena,
            this.teams.toList(),
            this.playerData,
            this.mapConfig,
            level
        )

        handler = game
        game.start()
    }

    override fun forceStart() {
        arenaPlayers.values.forEach { p ->
            if (p.name.toLowerCase() !in playerData) {
                selectTeam(p)
            }
        }

        prepareGame()
    }

    override fun leaveArena(p: Player) {
        if (playerData.containsKey(p.name.toLowerCase())) {
//            signManager.updateTeamSigns() //TODO: sign support
        }

        playerData.remove(p.name.toLowerCase())

        super.leaveArena(p)
    }

    override fun messageAllPlayers(message: String, player: Player, data: BedWarsData?) {
        val pData = data ?: playerData[player.name.toLowerCase()]

        if (pData != null) {
            val msg = configuration.allFormat.translatePlaceholders(
                player,
                context,
                pData.team.context,
                MessageScope.getContext(player, message)
            )

            arenaPlayers.values.forEach {
                it.sendMessage(msg)
            }
        } else {
            super.messageAllPlayers(message, player, data)
        }
    }
}