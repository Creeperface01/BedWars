package com.creeperface.nukkit.bedwars.arena

import cn.nukkit.scheduler.Task
import com.creeperface.nukkit.bedwars.api.arena.Arena.ArenaState


class ArenaSchedule(var plugin: Arena) : Task() {

    var gameTime = 0
    var startTime = plugin.startTime
    var drop = 0

    override fun onRun(tick: Int) {
        if (this.plugin.starting) {
            this.starting()
        } else if (this.plugin.gameState == ArenaState.GAME && !this.plugin.ending) {
            this.game()
        }
    }

    private fun starting() {
        if (this.startTime == 5) {
            this.plugin.selectMap()
        }

        if (this.startTime <= 0) {
            this.plugin.startGame()
            return
        }

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

        if (gameTime > 3600) {
            plugin.stopGame()
        }

        this.drop++
    }

    fun ending() {

    }
}
