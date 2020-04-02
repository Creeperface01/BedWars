package com.creeperface.nukkit.bedwars.arena.manager

import cn.nukkit.Player
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.arena.Arena.ArenaState
import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.utils.configuration

class VotingManager(val plugin: Arena) {

    var players = mutableMapOf<String, Int>()
    lateinit var currentTable: Array<String>
    lateinit var stats: Array<Int>

    fun createVoteTable() {
        var maps: List<MapConfiguration> = BedWars.instance.maps.values.toMutableList()

        with(plugin.mapFilter) {
            if (enable) {
                maps = maps.filter {
                    var accepted = true

                    if (this.teamCount.isNotEmpty() && it.teams.size !in this.teamCount) {
                        accepted = false
                    }

                    if (this.include.isNotEmpty() && !this.include.contains(it.name)) {
                        accepted = false
                    }

                    if (this.exclude.isNotEmpty() && this.exclude.contains(it.name)) {
                        accepted = false
                    }

                    accepted
                }
            }
        }

        val all = maps.map { it.name }.shuffled()

        val table = mutableListOf<String>()

        for (i in 0..all.size.coerceAtMost(configuration.votesSize)) {
            table.add(all[i])
        }

        this.currentTable = table.toTypedArray()
        this.stats = Array(this.currentTable.size) { 0 }

        this.players.clear()
    }

    fun onVote(p: Player, vote: String) {
        if (!this.plugin.voting || this.plugin.arenaState == ArenaState.GAME || !this.plugin.inArena(p)) {
            p.sendMessage(BedWars.prefix + (Lang.CAN_NOT_VOTE.translate()))
            return
        }

        val index = try {
            vote.toInt()
        } catch (e: NumberFormatException) {
            this.currentTable.withIndex().firstOrNull { it.value.equals(vote, true) }?.index ?: -1
        }

        if (index < 0 || index >= currentTable.size) {
            p.sendMessage(BedWars.prefix + (Lang.USE_VOTE.translate()))
            return
        }

        var oldIndex = -1

        this.players[p.name.toLowerCase()]?.let {
            this.stats[it]--
            oldIndex = it
        }

        this.stats[index]++

        this.players[p.name.toLowerCase()] = index
        p.sendMessage(BedWars.prefix + (Lang.VOTE.translate(this.currentTable[index])))

        plugin.scoreboardManager.updateVote(index)
        if (oldIndex >= 0) {
            plugin.scoreboardManager.updateVote(oldIndex)
        }
    }
}