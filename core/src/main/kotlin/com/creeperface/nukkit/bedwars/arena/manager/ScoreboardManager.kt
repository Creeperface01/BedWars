package com.creeperface.nukkit.bedwars.arena.manager

import cn.nukkit.Player
import com.creeperface.nukkit.bedwars.arena.Arena
import gt.creeperface.nukkit.scoreboardapi.ScoreboardAPI
import cn.nukkit.utils.TextFormat as TF

class ScoreboardManager(private val arena: Arena) {

    private val scoreboard = ScoreboardAPI.builder().build()

    fun addPlayer(p: Player) {
        scoreboard.addPlayer(p)
    }

    fun removePlayer(p: Player) {
        scoreboard.removePlayer(p)
    }

    fun initVotes() {
        scoreboard.resetAllScores()

        scoreboard.setDisplayName("${TF.DARK_GRAY}Voting ${TF.WHITE}| ${TF.GOLD}/vote <map>")
        val vm = arena.votingManager
        val votes = vm.currentTable

        for (i in votes.indices) {
            scoreboard.setScore(i.toLong(), "${TF.AQUA}[${i + 1}] ${TF.DARK_GRAY}${votes[i]} ${TF.RED}»${TF.GREEN}${vm.stats[i]} votes", i)
        }

        scoreboard.update()
    }

    fun updateVote(index: Int) {
        val vm = arena.votingManager
        val votes = vm.currentTable

        scoreboard.setScore(index.toLong(), "${TF.AQUA}[${index + 1}] ${TF.DARK_GRAY}${votes[index]} ${TF.RED}»${TF.GREEN}${vm.stats[index]} votes", index)

        scoreboard.update()
    }

    fun initGame() {
        scoreboard.resetAllScores()

        val map = arena.map ?: "Voting"
        scoreboard.setDisplayName("${TF.DARK_GRAY}Map: ${TF.GOLD}$map")

        arena.teams.forEachIndexed { index, team ->
            scoreboard.setScore(index.toLong(), team.status, index)
        }

        scoreboard.update()
    }

    fun updateTeam(index: Int) {
        scoreboard.setScore(index.toLong(), arena.teams[index].status, index)
        scoreboard.update()
    }

    fun reset() {
        scoreboard.resetAllScores()
        scoreboard.update()
    }
}