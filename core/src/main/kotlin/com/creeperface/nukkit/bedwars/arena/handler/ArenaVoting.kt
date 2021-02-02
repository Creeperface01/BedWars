package com.creeperface.nukkit.bedwars.arena.handler

import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.arena.handler.LobbyHandler
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.arena.ArenaState
import com.creeperface.nukkit.bedwars.arena.manager.VotingManager
import com.creeperface.nukkit.bedwars.task.WorldCopyTask

class ArenaVoting(
    arena: Arena
) : ArenaLobby(arena), LobbyHandler.VotingHandler {

    val votingManager = VotingManager(this)

    override val state = ArenaState.VOTING

    init {
        this.votingManager.initVotes()
        this.scoreboardManager.initVotes(this)
    }

    fun selectMap(force: Boolean = false): MapConfiguration {
        val stats = this.votingManager.stats
        val map = this.votingManager.currentTable[stats.indices.maxByOrNull { stats[it] }!!]

        val levelName = map.name + "_" + this.name

        if (this.plugin.server.isLevelLoaded(levelName)) {
            this.plugin.server.unloadLevel(this.plugin.server.getLevelByName(levelName))
        }

        WorldCopyTask(this.plugin, map.name, this.name, force)

        messageAllPlayers(Lang.SELECT_MAP, map.name)

        return map
    }

    override fun forceStart() {
        val teamSelect = ArenaTeamSelect(arena, selectMap(true))
        handler = teamSelect
    }

    override fun checkLobby() {
        if (this.arenaPlayers.size >= this.voteConfig.players) {
            task.voteTime = voteConfig.countdown
        } else {
            task.voteTime = 0
        }
    }
}