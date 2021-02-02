package com.creeperface.nukkit.bedwars.arena.handler.listener

import cn.nukkit.Player
import cn.nukkit.event.EventHandler
import cn.nukkit.event.EventPriority
import cn.nukkit.event.Listener
import cn.nukkit.event.block.BlockBreakEvent
import cn.nukkit.event.block.BlockPlaceEvent
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.player.PlayerChatEvent
import cn.nukkit.event.player.PlayerInteractEvent
import com.creeperface.nukkit.bedwars.api.utils.handle
import com.creeperface.nukkit.bedwars.arena.ArenaState
import com.creeperface.nukkit.bedwars.arena.handler.ArenaLobby

class ArenaLobbyListener(
    val arena: ArenaLobby
) : Listener {

    @EventHandler
    fun onEntityDamage(e: EntityDamageEvent) {
        val p = e.entity

        if (p is Player && arena.inArena(p)) {
            e.isCancelled = true
            return
        }

        if (e is EntityDamageByEntityEvent) {
            val damager = e.damager

            if (damager is Player && arena.inArena(damager)) {
                e.isCancelled = true
                return
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onPlayerInteract(e: PlayerInteractEvent) {
        val p = e.player

        if (!arena.inArena(p)) {
            return
        }

        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            val slot = p.inventory.heldItemIndex
            if (slot == arena.lobbyItem?.slot) {
                this.arena.leaveArena(p)
                p.teleport(arena.plugin.server.defaultLevel.spawnLocation)
                return
            }

            arena.handle(ArenaState.TEAM_SELECT) {
                this.showTeamSelection(p)
            }

            arena.handle(ArenaState.VOTING) {
                this.votingManager.showVotingSelection(p)
            }

            return
        }

        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            arena.handle(ArenaState.TEAM_SELECT) {
                isJoinSign(e.block)?.let { team ->
                    addToTeam(p, team)
                    return
                }
            }
        }
    }

    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        if (arena.inArena(e.player)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        if (arena.inArena(e.player)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onChat(e: PlayerChatEvent) {
        val p = e.player

        if (!arena.inArena(p)) {
            return
        }

        e.setCancelled()

        this.arena.messageAllPlayers(e.message, p)
    }
}