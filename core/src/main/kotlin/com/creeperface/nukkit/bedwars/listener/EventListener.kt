package com.creeperface.nukkit.bedwars.listener

import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerAsyncPreLoginEvent
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.event.player.PlayerQuitEvent
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.data.Stats
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityArenaSign
import com.creeperface.nukkit.bedwars.obj.GlobalData
import com.creeperface.nukkit.bedwars.utils.Configuration
import com.creeperface.nukkit.bedwars.utils.blockEntity
import com.creeperface.nukkit.bedwars.utils.identifier
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.function.Consumer

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
    fun onAsyncLogin(e: PlayerAsyncPreLoginEvent) {
        runBlocking {
            val identifier = when (plugin.configuration.playerIdentifier) {
                Configuration.PlayerIdentifier.UUID -> e.uuid.toString()
                Configuration.PlayerIdentifier.NAME -> e.name
            }

            val stats = plugin.dataProvider.getData(identifier)

            if (stats == null) {
                plugin.dataProvider.register(e.name, identifier)
            }

            e.scheduledActions.add(Consumer {
                val po = it.getPlayer(e.uuid)

                po.ifPresent { p ->
                    plugin.players[p.id] = GlobalData(p, stats ?: Stats.initial())
                }
            })
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val data = plugin.players.remove(e.player.id) ?: return

        GlobalScope.launch {
            plugin.dataProvider.saveData(e.player.identifier, data.stats)
        }
    }
}