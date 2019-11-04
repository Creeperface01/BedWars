package com.creeperface.nukkit.bedwars.arena

import cn.nukkit.scheduler.Task
import java.util.*


class ArenaSchedule(var plugin: Arena) : Task() {

    var gameTime = 0
    var startTime = 50
    var sign = 0
    var drop = 0

    override fun onRun(tick: Int) {
        if (this.plugin.starting) {
            this.starting()
        } else if (this.plugin.game == Arena.ArenaState.GAME && !this.plugin.ending) {
            this.game()
        }
    }

    fun waiting() {
        val count = this.plugin.playerData.size
        for (p in ArrayList(this.plugin.playerData.values)) {
            p.player.sendPopup("§eCekam na hrace... §b(§c$count/§a16§b)")
        }
    }

    fun starting() {
        if (this.startTime == 5) {
            this.plugin.selectMap()
        }

        if (this.startTime <= 0) {
            this.plugin.startGame()
            return
        }

        this.startTime--
    }

    fun game() {
        gameTime++
        this.plugin.dropBronze()

        if (this.drop % 7 == 0) {
            this.plugin.dropIron()
        }

        if (this.drop % 30 == 0) {
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
