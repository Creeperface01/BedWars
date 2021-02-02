package com.creeperface.nukkit.bedwars.arena.handler

import cn.nukkit.AdventureSettings
import cn.nukkit.Player
import cn.nukkit.block.Block
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.api.utils.applyFilter
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.arena.IArena
import com.creeperface.nukkit.bedwars.arena.Team
import com.creeperface.nukkit.bedwars.arena.handler.listener.ArenaLobbyListener
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityTeamSign
import com.creeperface.nukkit.bedwars.obj.BedWarsData
import com.creeperface.nukkit.bedwars.utils.blockEntity
import com.creeperface.nukkit.bedwars.utils.configuration
import com.creeperface.nukkit.bedwars.utils.unregisterAll
import com.creeperface.nukkit.placeholderapi.api.scope.MessageScope
import com.creeperface.nukkit.placeholderapi.api.util.translatePlaceholders

@Suppress("LeakingThis")
abstract class ArenaLobby(val arena: Arena) : IArena by arena {

    override var closed = false

    val listener = ArenaLobbyListener(this)

    init {
        val plugin = this.arena.plugin
        plugin.server.pluginManager.registerEvents(this.listener, plugin)
    }

    override fun joinToArena(p: Player): Boolean {
        val globalData =
            plugin.players[p.id] ?: error("Trying to to join arena when not having global player data set")
        globalData.arena = this.arena

        arenaPlayers[p.name.toLowerCase()] = p

        p.nameTag = p.name //TODO: nametag config
        p.sendMessage(Lang.JOIN.translatePrefix(this.name))
        p.teleport(this.arenaLobby)
        p.setSpawn(this.arenaLobby)

        scoreboardManager.addPlayer(p)

        val inv = p.inventory
        inv.clearAll()

        teamSelectItem?.set(inv)
        voteConfig.item?.set(inv)
        lobbyItem?.set(inv)

        inv.sendContents(p)

        p.adventureSettings.set(AdventureSettings.Type.ALLOW_FLIGHT, false)
        p.gamemode = 3
        p.setGamemode(0)

        this.checkLobby()
        this.signManager.updateMainSign()
        return true
    }

    abstract fun checkLobby()

    fun selectRandomMap(): MapConfiguration? {
        return arena.mapFilter.applyFilter(BedWars.instance.maps.values).randomOrNull()
    }

    fun isJoinSign(b: Block): Team? {
        return (b.blockEntity as? BlockEntityTeamSign)?.team
    }

    override fun leaveArena(p: Player) {
        arena.leaveArena(p)
        checkLobby()
    }

    override fun messageAllPlayers(message: String, player: Player, data: BedWarsData?) {
        val msg =
            configuration.lobbyFormat.translatePlaceholders(
                player,
                context,
                MessageScope.getContext(player, message)
            )

        arenaPlayers.values.forEach {
            it.sendMessage(msg)
        }
    }

    abstract fun forceStart()

    override fun close() {
        super.close()

        this.listener.unregisterAll()
    }
}