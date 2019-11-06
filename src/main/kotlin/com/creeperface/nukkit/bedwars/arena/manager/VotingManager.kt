package com.creeperface.nukkit.bedwars.arena.manager

import cn.nukkit.Player
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.obj.Language

class VotingManager(val plugin: Arena) {

    var players = mutableMapOf<String, Int>()
    lateinit var currentTable: Array<String>
    lateinit var stats: Array<Int>

    fun createVoteTable() {
        val all = BedWars.instance.maps.keys.toMutableList()
        all.shuffle()

        val table = mutableListOf<String>()

        for (i in 0..all.size.coerceAtMost(4)) {
            table.add(all[i])
        }

        this.currentTable = table.toTypedArray()
        this.stats = Array(this.currentTable.size) { 0 }

        this.players.clear()
    }

    fun onVote(p: Player, vote: String) {
        if (this.plugin.game == Arena.ArenaState.GAME || !this.plugin.inArena(p)) {
            p.sendMessage(BedWars.prefix + (Language.CAN_NOT_VOTE.translate2()))
            return
        }

        val index = try {
            vote.toInt()
        } catch (e: NumberFormatException) {
            this.currentTable.withIndex().firstOrNull { it.value.equals(vote, true) }?.index ?: -1
        }

        if (index < 0 || index >= currentTable.size) {
            p.sendMessage(BedWars.prefix + (Language.USE_VOTE.translate2()))
            return
        }

        var oldIndex = -1

        this.players[p.name.toLowerCase()]?.let {
            this.stats[it]--
            oldIndex = it
        }

        this.stats[index]++

        this.players[p.name.toLowerCase()] = index
        p.sendMessage(BedWars.prefix + (Language.VOTE.translate2(this.currentTable[index])))

        plugin.scoreboardManager.updateVote(index)
        if (oldIndex >= 0) {
            plugin.scoreboardManager.updateVote(oldIndex)
        }
    }
}