package com.creeperface.nukkit.bedwars.arena

import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.api.arena.Arena.ArenaState
import com.creeperface.nukkit.bedwars.api.arena.Team
import com.creeperface.nukkit.bedwars.api.arena.configuration.IArenaConfiguration
import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.shop.ShopMenuWindow
import com.creeperface.nukkit.bedwars.api.shop.ShopWindow
import com.creeperface.nukkit.bedwars.obj.BedWarsData
import com.creeperface.nukkit.bedwars.shop.inventory.MenuWindow
import com.creeperface.nukkit.bedwars.utils.EnderChestInventory
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.util.*

class Team(override val arena: Arena,
           override val id: Int,
           config: IArenaConfiguration.TeamConfiguration
) : Team, IArenaConfiguration.ITeamConfiguration by config {

    private var bed = true

    var status = ""
        private set

    override val enderChest = EnderChestInventory()

    override val shop: MenuWindow = TODO()

    val windowMap = Int2ObjectOpenHashMap<ShopWindow>()

    val players = mutableMapOf<String, BedWarsData>()

    var mapConfig: MapConfiguration.TeamData

    init {
        recalculateStatus()

        fun foreachWindows(window: ShopMenuWindow) {
            window.windows.values.forEach {
                windowMap[it.id] = it

                if (it is ShopMenuWindow) {
                    foreachWindows(it)
                }
            }
        }

        foreachWindows(shop)
    }

    override fun getTeamPlayers() = players.toMap()

    override fun hasBed() = this.bed

    override fun messagePlayers(message: String) {
        for (p in ArrayList(this.players.values)) {
            p.player.sendMessage(message)
        }
        this.arena.plugin.server.logger.info(message)
    }

    fun messagePlayers(message: String, data: BedWarsData) {
        val player = data.player

        val msg = TextFormat.GRAY.toString() + "[" + chatColor + "Team" + TextFormat.GRAY + "]   " + player.displayName /*+ data.baseData.chatColor*/ + ": " + message //TODO: chat color

        for (p in ArrayList(this.players.values)) {
            p.player.sendMessage(msg)
        }
        this.arena.plugin.server.logger.info(msg)
    }

    fun addPlayer(p: BedWarsData) {
        this.players[p.player.name.toLowerCase()] = p
        p.team = this
        p.player.nameTag = chatColor.toString() + p.player.name
        p.player.displayName = TextFormat.GRAY.toString() /*+ "[" + TextFormat.GREEN + p.baseData.level + TextFormat.GRAY + "]" + p.baseData.prefix*/ + " " + p.team.chatColor + p.player.name + TextFormat.RESET //TODO: chat color
        recalculateStatus()
    }

    fun removePlayer(data: BedWarsData) {
        this.players.remove(data.player.name.toLowerCase())
        data.player.nameTag = data.player.name
        recalculateStatus()

        if (arena.gameState == ArenaState.GAME) {
            arena.scoreboardManager.updateTeam(this.id)
        }
    }

    fun onBedBreak() {
        this.bed = false
        recalculateStatus()
        arena.scoreboardManager.updateTeam(this.id)
    }

    private fun recalculateStatus() {
        val count = this.players.size
        val bed = hasBed()

        if (count >= 1 || bed) {
            this.status = "                                          " + chatColor + name + ": " + (if (bed) TextFormat.GREEN.toString() + "✔" else TextFormat.RED.toString() + "✖") + TextFormat.GRAY + " " + this.players.size + "\n"
        } else {
            this.status = ""
        }
    }
}
