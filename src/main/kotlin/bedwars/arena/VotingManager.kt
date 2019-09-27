package bedwars.arena

import bedwars.BedWars
import bedwars.obj.Language
import cn.nukkit.Player
import org.apache.commons.lang3.math.NumberUtils
import java.util.*

class VotingManager(var plugin: Arena) {

    var players = mutableMapOf<String, String>()
    var currentTable = arrayOfNulls<String>(4)
    var stats = mutableMapOf<String, Int>()

    fun createVoteTable() {
        val all = ArrayList(BedWars.instance!!.maps.keys)
        all.shuffle()

        var i = 0
        val table = ArrayList<String>()

        while (i < 4 && i < all.size) {
            table.add(all[i])
            i++
        }

        this.currentTable = table.toArray(emptyArray())

        this.stats = HashMap()
        for (l in currentTable.indices) {
            stats[currentTable[l]!!] = 0
        }

        this.players.clear()
    }

    fun onVote(p: Player, vote: String) {
        @Suppress("NAME_SHADOWING")
        var vote = vote
        if (this.plugin.game != 0 || !this.plugin.inArena(p)) {
            p.sendMessage(BedWars.prefix + Language.translate("can_not_vote"))
            return
        }

        if (NumberUtils.isNumber(vote)) {
            val intValue = Integer.valueOf(vote)

            if (!(intValue <= currentTable.size && intValue > 0)) {
                p.sendMessage(BedWars.prefix + Language.translate("use_vote"))
                return
            }
            if (this.players.containsKey(p.name.toLowerCase())) {
                this.stats[this.players[p.name.toLowerCase()]!!] = this.stats[this.players[p.name.toLowerCase()]]!! - 1
            }
            this.stats[currentTable[intValue - 1]!!] = this.stats[currentTable[intValue - 1]]!! + 1
            this.players[p.name.toLowerCase()] = currentTable[intValue - 1]!!
            p.sendMessage(BedWars.prefix + Language.translate("vote", this.currentTable[intValue - 1]!!))
            plugin.barUtil.updateVotes()
        } else {
            vote = vote.toLowerCase()
            var found = false

            for (s in this.currentTable) {
                if (vote.equals(s, ignoreCase = true)) {
                    vote = s!!
                    found = true
                    break
                }
            }

            if (!found) {
                p.sendMessage(BedWars.prefix + Language.translate("use_vote"))
                return
            }

            if (this.players.containsKey(p.name.toLowerCase())) {
                this.stats[this.players[p.name.toLowerCase()]!!] = this.stats[this.players[p.name.toLowerCase()]]!! - 1
            }

            val finall = Character.toUpperCase(vote[0]) + vote.substring(1)
            this.stats[finall] = this.stats[finall]!! + 1
            this.players[p.name.toLowerCase()] = finall
            p.sendMessage(BedWars.prefix + Language.translate("vote", vote))
            plugin.barUtil.updateVotes()
        }
    }
}