package com.creeperface.nukkit.bedwars.arena

import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.placeholder.TeamScope
import com.creeperface.nukkit.bedwars.api.shop.ShopMenuWindow
import com.creeperface.nukkit.bedwars.api.shop.ShopWindow
import com.creeperface.nukkit.bedwars.api.utils.handle
import com.creeperface.nukkit.bedwars.obj.BedWarsData
import com.creeperface.nukkit.bedwars.shop.inventory.MenuWindow
import com.creeperface.nukkit.bedwars.utils.*
import com.creeperface.nukkit.placeholderapi.api.scope.Message
import com.creeperface.nukkit.placeholderapi.api.scope.MessageScope
import com.creeperface.nukkit.placeholderapi.api.util.translatePlaceholders
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.util.*

class Team(
    override val arena: Arena,
    override val id: Int,
    config: MapConfiguration.TeamData
) : APITeam, MapConfiguration.ITeamData by config {

    private var bed = true

    var status = ""
        private set

    override val enderChest = EnderChestInventory()

    val windowMap = Int2ObjectOpenHashMap<ShopWindow>()

    val players = mutableMapOf<String, BedWarsData>()

    override val context = TeamScope.getContext(this)

    override val shop: MenuWindow

    init {
        refreshStatus()

        fun foreachWindows(window: ShopMenuWindow) {
            window.windows.values.forEach {
                windowMap[it.id] = it

                if (it is ShopMenuWindow) {
                    foreachWindows(it)
                }
            }
        }

        shop = arena.plugin.shop.load(arena, this)
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

        val msg = configuration.teamFormat.translatePlaceholders(
            player,
            context,
            MessageScope.getContext(Message(player, message))
        )

        for (p in ArrayList(this.players.values)) {
            p.player.sendMessage(msg)
        }
        this.arena.plugin.server.logger.info(msg)
    }

    fun addPlayer(p: BedWarsData) {
        this.players[p.player.name.toLowerCase()] = p
        p.player.nameTag = chatColor.toString() + p.player.name
        refreshStatus()
    }

    fun removePlayer(data: BedWarsData) {
        this.players.remove(data.player.name.toLowerCase())
        data.player.nameTag = data.player.name
        refreshStatus()

        arena.handle(ArenaState.GAME) {
            arena.scoreboardManager.updateTeam(this, this@Team.id)
        }
    }

    override fun isAlive() = bed || players.isNotEmpty()

    fun onBedBreak() {
        this.bed = false
        refreshStatus()

        arena.handle(ArenaState.GAME) {
            arena.scoreboardManager.updateTeam(this, this@Team.id)
        }
    }

    private fun refreshStatus() {
        val count = this.players.size
        val bed = hasBed()

        if (count >= 1 || bed) {
            this.status =
                "$chatColor$name: " + (if (bed) {
                    BED_ALIVE_COLOR + BED_ALIVE_CHAR
                } else {
                    BED_DESTROYED_COLOR + BED_DESTROYED_CHAR
                }) + TF.GRAY + " " + this.players.size + "    "
        } else {
            this.status = ""
        }
    }

    companion object {

        const val BED_ALIVE_CHAR = '✔'
        val BED_ALIVE_COLOR = TF.GREEN

        const val BED_DESTROYED_CHAR = '✖'
        val BED_DESTROYED_COLOR = TF.RED
    }
}
