package com.creeperface.nukkit.bedwars.listener

import cn.nukkit.block.BlockSignPost
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.event.EventHandler
import cn.nukkit.event.EventPriority
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.arena.Team
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityArenaSign
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityTeamSign
import com.creeperface.nukkit.bedwars.utils.fullChunk
import com.creeperface.nukkit.bedwars.utils.register
import java.util.*

class CommandEventListener(private val plugin: BedWars) : Listener {

    val actionPlayers = mutableMapOf<UUID, ActionData>()

    fun register() {
        register(plugin, this::onInteract, EventPriority.LOW, true)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onInteract(e: PlayerInteractEvent) {
        val p = e.player
        val b = e.block

        val actionData = actionPlayers[p.uniqueId] ?: return

        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            if (b is BlockSignPost) {
                when (actionData.action) {
                    Action.SET_SIGN -> {
                        val data = actionData.data as Arena

                        BlockEntityArenaSign(
                                b.fullChunk,
                                BlockEntity.getDefaultCompound(b, BlockEntityArenaSign.NETWORK_ID)
                                        .putString("bw-arena", data.name)
                        )
                        actionPlayers.remove(p.uniqueId)
                        p.sendMessage(BedWars.prefix + TextFormat.GREEN + "Sign successfully set")
                    }
                    Action.SET_TEAM_SIGN -> {
                        val data = actionData.data as Team

                        BlockEntityTeamSign(
                                b.fullChunk,
                                BlockEntity.getDefaultCompound(b, BlockEntityTeamSign.NETWORK_ID)
                                        .putString("bw-arena", data.arena.name)
                                        .putInt("bw-team", data.id)
                        )
                        actionPlayers.remove(p.uniqueId)
                        p.sendMessage(BedWars.prefix + TextFormat.GREEN + "Sign successfully set")
                    }
                }
            }
        }
    }

    data class ActionData(
            val action: Action,
            val data: Any?
    )

    enum class Action {
        SET_SIGN,
        SET_TEAM_SIGN
    }
}