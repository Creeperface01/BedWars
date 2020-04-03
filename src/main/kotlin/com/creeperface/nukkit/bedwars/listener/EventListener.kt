package com.creeperface.nukkit.bedwars.listener

import cn.nukkit.event.EventHandler
import cn.nukkit.event.EventPriority
import cn.nukkit.event.Listener
import cn.nukkit.event.player.*
import cn.nukkit.event.server.DataPacketReceiveEvent
import cn.nukkit.network.protocol.ProtocolInfo
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.data.Stats
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityArenaSign
import com.creeperface.nukkit.bedwars.obj.GlobalData
import com.creeperface.nukkit.bedwars.utils.PlayerIdentifier
import com.creeperface.nukkit.bedwars.utils.blockEntity
import com.creeperface.nukkit.bedwars.utils.configuration
import com.creeperface.nukkit.bedwars.utils.identifier
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class EventListener(private val plugin: BedWars) : Listener {

    init {
        if (configuration.autoJoin) {
            plugin.server.pluginManager.registerEvent(DataPacketReceiveEvent::class.java, this, EventPriority.NORMAL, { _, e ->
                onPacketReceive(e as DataPacketReceiveEvent)
            }, plugin)
        }
    }

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
        if (!configuration.savePlayerData) {
            e.scheduleSyncAction {
                val po = it.getPlayer(e.uuid)

                po.ifPresent { p ->
                    plugin.players[p.id] = GlobalData(p, Stats.initial())
                }
            }
            return
        }

        runBlocking {
            val identifier = when (plugin.configuration.playerIdentifier) {
                PlayerIdentifier.UUID -> e.uuid.toString()
                PlayerIdentifier.NAME -> e.name
            }

            val stats = plugin.dataProvider.getData(identifier)

            if (stats == null) {
                plugin.dataProvider.register(e.name, identifier)
            }

            e.scheduleSyncAction {
                val po = it.getPlayer(e.uuid)

                po.ifPresent { p ->
                    plugin.players[p.id] = GlobalData(p, stats ?: Stats.initial())
                }
            }
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val data = plugin.players.remove(e.player.id) ?: return

        if (configuration.savePlayerData) {
            GlobalScope.launch {
                plugin.dataProvider.saveData(e.player.identifier, data.stats)
            }
        }
    }

    private fun onPacketReceive(e: DataPacketReceiveEvent) {
        if (e.packet.pid() == ProtocolInfo.SET_LOCAL_PLAYER_AS_INITIALIZED_PACKET) {
            val p = e.player
            val data = plugin.players[p.id] ?: return

            if (data.arena != null) {
                return
            }

            plugin.joinRandomArena(p)
        }
    }

    @EventHandler
    fun onCommandPreprocess(e: PlayerCommandPreprocessEvent) {
        val p = e.player

        if (!configuration.disableCommands) {
            return
        }

        val cmd = e.message.substring(1).split(" ").firstOrNull()?.toLowerCase() ?: return

        if (cmd == "bw" || cmd == "bedwars" || cmd in configuration.enabledCommands) {
            return
        }

        if (plugin.getPlayerArena(p) != null) {
            e.setCancelled()
            p.sendMessage(Lang.NOT_GAME_COMMAND.translate())
        }
    }

    @EventHandler
    fun onChat(e: PlayerChatEvent) {
        if (!configuration.separateChat) {
            return
        }

        val p = e.player

        if (plugin.getPlayerArena(p) != null) {
            return
        }

        val inArena = plugin.players.values.filter { it.arena != null }.map { it.player }
        e.recipients.removeAll(inArena)
    }
}