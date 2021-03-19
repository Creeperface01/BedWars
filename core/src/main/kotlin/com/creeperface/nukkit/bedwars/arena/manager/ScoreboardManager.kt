package com.creeperface.nukkit.bedwars.arena.manager

import cn.nukkit.Player
import com.creeperface.nukkit.bedwars.arena.Team
import com.creeperface.nukkit.bedwars.arena.handler.ArenaGame
import com.creeperface.nukkit.bedwars.arena.handler.ArenaTeamSelect
import com.creeperface.nukkit.bedwars.arena.handler.ArenaVoting
import com.creeperface.nukkit.bedwars.utils.plus
import com.creeperface.nukkit.bedwars.utils.ucFirst
import gt.creeperface.nukkit.scoreboardapi.ScoreboardAPI
import cn.nukkit.utils.TextFormat as TF

class ScoreboardManager {

    private val scoreboard = ScoreboardAPI.builder().build()

    fun addPlayer(p: Player) {
        scoreboard.addPlayer(p)
    }

    fun removePlayer(p: Player) {
        scoreboard.removePlayer(p)
    }

    fun initVotes(arena: ArenaVoting) {
        scoreboard.resetAllScores()

        scoreboard.setDisplayName("${TF.DARK_GRAY}Voting ${TF.WHITE}| ${TF.GOLD}/vote <map>")
        val vm = arena.votingManager
        val votes = vm.currentTable

        votes.forEachIndexed { i, map ->
            scoreboard.setScore(
                i.toLong(),
                "${TF.AQUA}[${i + 1}] ${TF.DARK_GRAY}${map.name} ${TF.RED}» ${TF.GREEN}${vm.stats[i]} votes",
                i
            )
        }

        scoreboard.setScore(
            votes.size.toLong(),
            "${TF.DARK_GRAY}Time: ${TF.GOLD} ${arena.voteConfig.countdown} s",
            votes.size
        )

        scoreboard.update()
    }

    fun updateVoteTime(arena: ArenaVoting) {
        val votes = arena.votingManager.currentTable
        scoreboard.setScore(votes.size.toLong(), "${TF.DARK_GRAY}Time: ${TF.GOLD} ${arena.task.voteTime} s", votes.size)

        scoreboard.update()
    }

    fun updateVote(arena: ArenaVoting, index: Int) {
        val vm = arena.votingManager
        val votes = vm.currentTable

        scoreboard.setScore(
            index.toLong(),
            "${TF.AQUA}[${index + 1}] ${TF.DARK_GRAY}${votes[index].name} ${TF.RED}» ${TF.GREEN}${vm.stats[index]} votes",
            index
        )

        scoreboard.update()
    }

    fun initTeamSelect(arena: ArenaTeamSelect) {
        scoreboard.resetAllScores()

        scoreboard.setDisplayName("${TF.DARK_GRAY}Map: ${TF.GOLD}${arena.mapConfig.name}")

        val teams = arena.teams

        teams.forEach {
            scoreboard.setScore(
                it.id.toLong(),
                it.chatColor + it.name.ucFirst() + TF.DARK_GRAY + ": " + TF.GREEN + it.players.size,
                it.id
            )
        }

        scoreboard.setScore(teams.size.toLong(), "${TF.DARK_GRAY}Time: ${TF.GOLD} ${arena.startTime} s", teams.size)

        scoreboard.update()
    }

    fun updateTeamPlayerCount(team: Team) {
        scoreboard.setScore(
            team.id.toLong(),
            team.chatColor + team.name.ucFirst() + TF.DARK_GRAY + ": " + TF.GREEN + team.players.size,
            team.id
        )
    }

    fun updateStartTime(arena: ArenaTeamSelect) {
        val teams = arena.teams

        scoreboard.setScore(
            teams.size.toLong(),
            "${TF.DARK_GRAY}Time: ${TF.GOLD} ${arena.task.startTime} s",
            teams.size
        )
        scoreboard.update()
    }

    fun initGame(arena: ArenaGame) {
        scoreboard.resetAllScores()

        scoreboard.setDisplayName("${TF.DARK_GRAY}Map: ${TF.GOLD}${arena.mapConfig.name}")

        arena.teams.forEachIndexed { index, team ->
            scoreboard.setScore(index.toLong(), team.status, index)
        }

        scoreboard.update()
    }

    fun updateTeam(team: Team) {
        if (team.hasBed()) {
            scoreboard.setScore(team.id.toLong(), team.status, team.id)
        } else {
            scoreboard.resetScore(team.id.toLong())
        }
        scoreboard.update()
    }

    fun reset() {
        scoreboard.resetAllScores()
        scoreboard.update()
    }

    fun update() {
        scoreboard.update()
    }
}