package com.creeperface.nukkit.bedwars.arena

import cn.nukkit.scheduler.Task
import com.creeperface.nukkit.bedwars.api.arena.Arena.ArenaState
import com.creeperface.nukkit.bedwars.api.event.ArenaStopEvent


class ArenaTask(var plugin: Arena) : Task() {

    var gameTime = 0
    var startTime = plugin.startTime
    var drop = 0
    var voteTime = 0

    fun reset() {
        gameTime = 0
        startTime = plugin.startTime
        drop = 0
        voteTime = 0
    }

    override fun onRun(tick: Int) {
        if (voteTime > 0) {
            if (--voteTime == 0) {
                plugin.voting = false
                plugin.teamSelect = true
                plugin.selectMap()
            } else {
                plugin.scoreboardManager.updateVoteTime()
            }
        } else if (this.plugin.starting) {
            this.starting()
        } else if (this.plugin.arenaState == ArenaState.GAME && !plugin.ending) {
            this.game()
        }
    }

    private fun starting() {
        if (this.startTime == 5 && plugin.map == null) {
            this.plugin.selectMap()
        }

        if (this.startTime <= 0) {
            this.plugin.startGame()
            return
        }

        plugin.scoreboardManager.updateStartTime()

        this.startTime--
    }

    private fun game() {
        gameTime++

        if (this.drop % plugin.bronzeDropInterval == 0) {
            this.plugin.dropBronze()
        }

        if (this.drop % plugin.ironDropInterval == 0) {
            this.plugin.dropIron()
        }

        if (this.drop % plugin.goldDropInterval == 0) {
            this.plugin.dropGold()
        }

        if (gameTime > plugin.timeLimit) {
            plugin.stopGame(ArenaStopEvent.Cause.TIME_LIMIT)
        }

        this.drop++
    }
}
