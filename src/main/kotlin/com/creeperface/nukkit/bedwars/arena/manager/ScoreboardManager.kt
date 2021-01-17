package com.creeperface.nukkit.bedwars.arena.manager

import cn.nukkit.Player
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.arena.Team
import com.creeperface.nukkit.bedwars.utils.plus
import com.creeperface.nukkit.bedwars.utils.ucFirst
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
            scoreboard.setScore(i.toLong(), "${TF.AQUA}[${i + 1}] ${TF.DARK_GRAY}${votes[i]} ${TF.RED}» ${TF.GREEN}${vm.stats[i]} votes", i)
        }

        scoreboard.setScore(
            votes.size.toLong(),
            "${TF.DARK_GRAY}Time: ${TF.GOLD} ${arena.voteConfig.countdown} s",
            votes.size
        )

        scoreboard.update()
    }

    fun updateVoteTime() {
        val votes = arena.votingManager.currentTable
        scoreboard.setScore(votes.size.toLong(), "${TF.DARK_GRAY}Time: ${TF.GOLD} ${arena.task.voteTime} s", votes.size)

        scoreboard.update()
    }

    fun updateVote(index: Int) {
        val vm = arena.votingManager
        val votes = vm.currentTable

        scoreboard.setScore(index.toLong(), "${TF.AQUA}[${index + 1}] ${TF.DARK_GRAY}${votes[index]} ${TF.RED}» ${TF.GREEN}${vm.stats[index]} votes", index)

        scoreboard.update()
    }

    fun initTeamSelect() {
        scoreboard.resetAllScores()

        scoreboard.setDisplayName("${TF.DARK_GRAY}Map: ${TF.GOLD}${arena.map}")

        val teams = arena.teams
        teams.forEach {
            scoreboard.setScore(it.id.toLong(), it.chatColor + it.name.ucFirst() + TF.DARK_GRAY + ": " + TF.GREEN + it.players.size, it.id)
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

    fun updateStartTime() {
        val teams = arena.teams

        scoreboard.setScore(
            teams.size.toLong(),
            "${TF.DARK_GRAY}Time: ${TF.GOLD} ${arena.task.startTime} s",
            teams.size
        )
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

    fun update() {
        scoreboard.update()
    }
}