package com.creeperface.nukkit.bedwars.listener

import cn.nukkit.Player
import cn.nukkit.event.EventHandler
import cn.nukkit.event.EventPriority
import cn.nukkit.event.Listener
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.ProjectileLaunchEvent
import cn.nukkit.event.inventory.CraftItemEvent
import cn.nukkit.event.inventory.InventoryClickEvent
import cn.nukkit.event.inventory.InventoryPickupArrowEvent
import cn.nukkit.event.inventory.InventoryTransactionEvent
import cn.nukkit.event.player.*
import cn.nukkit.event.server.DataPacketReceiveEvent
import cn.nukkit.inventory.transaction.action.SlotChangeAction
import cn.nukkit.network.protocol.ProtocolInfo
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.arena.Arena
import com.creeperface.nukkit.bedwars.api.data.Stats
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityArenaSign
import com.creeperface.nukkit.bedwars.obj.GlobalData
import com.creeperface.nukkit.bedwars.shop.inventory.MenuWindow
import com.creeperface.nukkit.bedwars.shop.inventory.OfferWindow
import com.creeperface.nukkit.bedwars.shop.inventory.Window
import com.creeperface.nukkit.bedwars.utils.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class EventListener(private val plugin: BedWars) : Listener {

    fun register() {
        if (configuration.autoJoin) {
            register(plugin, this::onPacketReceive)
        }

        register(plugin, this::onInteract, ignoreCancelled = true)
        register(plugin, this::onAsyncLogin)
        register(plugin, this::onQuit)
        register(plugin, this::onCommandPreprocess)
        register(plugin, this::onChat)
        register(plugin, this::onArrowPickup, ignoreCancelled = true)
        register(plugin, this::onBucketFill, ignoreCancelled = true)
        register(plugin, this::onBucketEmpty, ignoreCancelled = true)
        register(plugin, this::onCraft, ignoreCancelled = true)
        register(plugin, this::onBedEnter, ignoreCancelled = true)
        register(plugin, this::onTransaction, ignoreCancelled = true)
        register(plugin, this::onSlotClick)
        register(plugin, this::onHungerChange, ignoreCancelled = true)
        register(plugin, this::onProjectileLaunch)
        register(plugin, this::onDamage, ignoreCancelled = true, priority = EventPriority.LOW)
        register(plugin, this::onDropItem)
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
            be.arena?.let { arena ->
                if (!arena.multiPlatform && p.loginChainData.deviceOS == 7 && !p.hasPermission("bedwars.crossplatform")) {
                    p.sendMessage(Lang.PE_ONLY.translate())
                    return
                }

                arena.joinToArena(p)
            }
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

    @EventHandler
    fun onCraft(e: CraftItemEvent) {
        if (plugin.getPlayerArena(e.player) != null) {
            e.setCancelled()
        }
    }

    @EventHandler
    fun onBedEnter(e: PlayerBedEnterEvent) {
        if (plugin.getPlayerArena(e.player) != null) {
            e.setCancelled()
        }
    }

    @EventHandler
    fun onArrowPickup(e: InventoryPickupArrowEvent) {
        val holder = e.inventory.holder as? Player ?: return

        if (plugin.getPlayerArena(holder) != null) {
            e.setCancelled()
        }
    }

    @EventHandler
    fun onBucketFill(e: PlayerBucketFillEvent) {
        if (plugin.getPlayerArena(e.player) != null) {
            e.setCancelled()
        }
    }

    @EventHandler
    fun onBucketEmpty(e: PlayerBucketEmptyEvent) {
        if (plugin.getPlayerArena(e.player) != null) {
            e.setCancelled()
        }
    }

    @EventHandler
    fun onTransaction(e: InventoryTransactionEvent) {
        val transaction = e.transaction

        for (action in transaction.actions) {
            if (action !is SlotChangeAction) {
                continue
            }

            if (action.inventory is Window) {
                e.setCancelled()
                return
            }
        }
    }

    @EventHandler
    fun onSlotClick(e: InventoryClickEvent) {
        val p = e.player

        val inv2 = e.inventory as? Window ?: return

        e.setCancelled()
        val slot = e.slot

        if (plugin.getPlayerArena(p) == null) {
            return
        }

        if (slot == inv2.size - 1) {
            inv2.parent?.let {
                p.openShopInventory(it)
            }

            return
        }

        if (inv2 is OfferWindow) {
            if (slot == 0) {
                plugin.shop.processTransaction(p, inv2.item, inv2.cost)
            }
        } else if (inv2 is MenuWindow) {
            val newWindow = inv2[slot]

            if (newWindow != null) {
                p.openShopInventory(newWindow)
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onHungerChange(e: PlayerFoodLevelChangeEvent) {
        val p = e.player
        val arena = plugin.getPlayerArena(p) ?: return

        if (arena.arenaState == Arena.ArenaState.LOBBY) {
            e.setCancelled()
        }
    }

    @EventHandler
    fun onProjectileLaunch(e: ProjectileLaunchEvent) {
        val p = e.entity.shootingEntity as? Player ?: return
        plugin.getPlayerArena(p) ?: return

        if (p.gamemode > 1) {
            e.setCancelled()
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onDamage(e: EntityDamageEvent) {
        val entity = e.entity

        if (entity is Player) {
            plugin.getPlayerArena(entity)?.let {
                if (it.arenaState == Arena.ArenaState.LOBBY) {
                    e.setCancelled()

                    if (e.cause == EntityDamageEvent.DamageCause.VOID) {
                        entity.teleport(it.arenaLobby)
                    }
                }
            }
        }
    }

    @EventHandler
    fun onDropItem(e: PlayerDropItemEvent) {
        plugin.getPlayerArena(e.player)?.let {
            e.setCancelled()
        }
    }
}