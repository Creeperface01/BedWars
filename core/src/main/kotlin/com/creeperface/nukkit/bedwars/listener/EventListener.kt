package com.creeperface.nukkit.bedwars.listener

import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerAsyncPreLoginEvent
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.event.player.PlayerPreLoginEvent
import cn.nukkit.event.player.PlayerQuitEvent
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityArenaSign
import com.creeperface.nukkit.bedwars.mysql.StatQuery
import com.creeperface.nukkit.bedwars.obj.GlobalData
import com.creeperface.nukkit.bedwars.utils.blockEntity
import kotlinx.coroutines.runBlocking

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

            if (!be.arena.multiPlatform && p.loginChainData.deviceOS == 7 && !p.hasPermission("bedwars.crossplatform")) {
                p.sendMessage(Lang.PE_ONLY.translate())
                return
            }

            be.arena.joinToArena(p)
        }
    }

    @EventHandler
    fun onPreLogin(e: PlayerPreLoginEvent) {
        val p = e.player
        plugin.players[p.id] = GlobalData(p)
    }

    @EventHandler
    fun onAsyncLogin(e: PlayerAsyncPreLoginEvent) {
        plugin.dataProvider?.let {
            runBlocking {
                val stats = it.getData(e.name)


            }
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val data = plugin.players.remove(e.player.id) ?: return

        StatQuery(plugin, data.stats)
    }
}