package com.creeperface.nukkit.bedwars.arena

import cn.nukkit.scheduler.Task
import com.creeperface.nukkit.bedwars.api.event.ArenaStopEvent
import com.creeperface.nukkit.bedwars.api.utils.handle
import com.creeperface.nukkit.bedwars.arena.handler.ArenaGame
import com.creeperface.nukkit.bedwars.arena.handler.ArenaTeamSelect

class ArenaTask(var plugin: Arena) : Task() {

    var gameTime = 0
    var startTime = plugin.startTime
    var drop = 0
    var voteTime = 0

    var onVoteEnd: (() -> Unit)? = null
    var onStart: (() -> Unit)? = null

    fun reset() {
        gameTime = 0
        startTime = plugin.startTime
        drop = 0
        voteTime = 0
    }

    override fun onRun(tick: Int) {
        plugin.handle(ArenaState.GAME) {
            if (!ending) {
                game(this)
            }

            return
        }

        if (voteTime > 0) {
            plugin.handle(ArenaState.VOTING) {
                if (--voteTime == 0) {
                    reset()
                    handler = ArenaTeamSelect(this@ArenaTask.plugin, selectMap())
                } else {
                    this@ArenaTask.plugin.scoreboardManager.updateVoteTime(this)
                }
            }
        } else {
            plugin.handle(ArenaState.TEAM_SELECT) {
                if (starting) {
                    starting(this)
                }
            }
        }
    }

    private fun starting(arena: ArenaTeamSelect) {
//        if (this.startTime == 5 && plugin.map == null) {
//            this.plugin.selectMap()
//        }
//        if (arena.fastStart && this.startTime > arena.fastStartTime && arena.arenaPlayers.size >= arena.fastStartPlayers) {
//            this.startTime = arena.fastStartTime
//        }

        if (this.startTime <= 0) {
            reset()
            arena.forceStart()
            return
        }

        plugin.scoreboardManager.updateStartTime(arena)

        this.startTime--
    }

    private fun game(arena: ArenaGame) {
        gameTime++

        if (this.drop % plugin.bronzeDropInterval == 0) {
            arena.dropBronze()
        }

        if (this.drop % plugin.ironDropInterval == 0) {
            arena.dropIron()
        }

        if (this.drop % plugin.goldDropInterval == 0) {
            arena.dropGold()
        }

        if (gameTime > plugin.timeLimit) {
            arena.stop(ArenaStopEvent.Cause.TIME_LIMIT)
        }

        this.drop++
    }
}
