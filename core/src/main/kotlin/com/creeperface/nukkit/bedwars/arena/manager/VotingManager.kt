package com.creeperface.nukkit.bedwars.arena.manager

import cn.nukkit.Player
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.api.utils.applyFilter
import com.creeperface.nukkit.bedwars.arena.handler.ArenaVoting
import com.creeperface.nukkit.bedwars.utils.configuration
import com.creeperface.nukkit.bedwars.utils.logError
import com.creeperface.nukkit.kformapi.KFormAPI
import com.creeperface.nukkit.kformapi.form.util.showForm
import kotlin.math.max

class VotingManager(val arena: ArenaVoting) {

    var players = mutableMapOf<String, Int>()
    lateinit var currentTable: Array<MapConfiguration>
    lateinit var stats: Array<Int>

    fun initVotes() {
        val maps = arena.mapFilter.applyFilter(BedWars.instance.maps.values).shuffled()

        if (maps.isEmpty()) {
            logError("Arena '${arena.name}' has empty map selection")
        }

        this.currentTable = maps.dropLast(max(0, maps.size - configuration.votesSize)).toTypedArray()
        this.stats = Array(this.currentTable.size) { 0 }

        this.players.clear()
    }

    fun onVote(p: Player, vote: String) {
        if (this.arena.closed || !this.arena.inArena(p)) {
            p.sendMessage(Lang.CAN_NOT_VOTE.translatePrefix())
            return
        }

        val index = vote.toIntOrNull()?.minus(1) ?: this.currentTable
            .withIndex().firstOrNull { it.value.name.equals(vote, true) }?.index ?: -1

        if (index < 0 || index >= currentTable.size) {
            p.sendMessage(Lang.USE_PREFIX.translatePrefix() + " " + Lang.CMD_VOTE_HELP.translate())
            return
        }

        var oldIndex = -1

        this.players[p.name.toLowerCase()]?.let {
            this.stats[it]--
            oldIndex = it
        }

        this.stats[index]++

        this.players[p.name.toLowerCase()] = index
        p.sendMessage(Lang.VOTE.translatePrefix(this.currentTable[index].name))

        arena.scoreboardManager.updateVote(this.arena, index)
        if (oldIndex >= 0) {
            arena.scoreboardManager.updateVote(this.arena, oldIndex)
        }
    }

    fun showVotingSelection(p: Player) {
        val form = KFormAPI.simpleForm {
            title(Lang.VOTING.translate())

            currentTable.forEach {
                button(it.name, it.icon) { p ->
                    onVote(p, it.name)
                }
            }
        }

        p.showForm(form)
    }
}