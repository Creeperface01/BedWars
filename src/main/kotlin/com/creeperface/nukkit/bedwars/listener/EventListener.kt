package com.creeperface.nukkit.bedwars.listener

import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.event.player.PlayerJoinEvent
import cn.nukkit.event.player.PlayerQuitEvent
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityArenaSign
import com.creeperface.nukkit.bedwars.mysql.JoinQuery
import com.creeperface.nukkit.bedwars.mysql.StatQuery
import com.creeperface.nukkit.bedwars.obj.GlobalData
import com.creeperface.nukkit.bedwars.obj.Language
import com.creeperface.nukkit.bedwars.utils.blockEntity

class EventListener(private val plugin: BedWars) : Listener {

    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        val p = e.player
        val b = e.block

        val data = plugin.players[p.id] ?: return

        if (data.arena != null) {
            return
        }

        val be = b.blockEntity
        if (be is BlockEntityArenaSign) {

            if (!be.arena.multiPlatform && p.loginChainData.deviceOS == 7 && !p.hasPermission("gameteam.helper")) { //TODO: permissions
                p.sendMessage(Language.PE_ONLY.translate2())
                return
            }

            be.arena.joinToArena(p)
        }
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val p = e.player
        plugin.players[p.id] = GlobalData(p)

        JoinQuery(plugin, p) //TODO: move into async login
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val data = plugin.players.remove(e.player.id) ?: return

        StatQuery(plugin, data.stats)
    }
}