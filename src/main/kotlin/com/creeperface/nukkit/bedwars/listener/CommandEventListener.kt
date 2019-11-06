package com.creeperface.nukkit.bedwars.listener

import cn.nukkit.block.BlockSignPost
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.event.EventHandler
import cn.nukkit.event.EventPriority
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityArenaSign
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityTeamSign
import com.creeperface.nukkit.bedwars.utils.fullChunk
import java.util.*

class CommandEventListener(private val plugin: BedWars) : Listener {

    val actionPlayers = mutableMapOf<UUID, Action>()

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onInteract(e: PlayerInteractEvent) {
        val p = e.player
        val b = e.block

        val action = actionPlayers[p.uniqueId] ?: return

        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            if (b is BlockSignPost) {
                when (action) {
                    Action.SET_SIGN -> {
                        BlockEntityArenaSign(b.fullChunk, BlockEntity.getDefaultCompound(b, BlockEntityArenaSign.NETWORK_ID))
                        actionPlayers.remove(p.uniqueId)
                        p.sendMessage(BedWars.prefix + TextFormat.GREEN + "Sign successfully set")
                    }
                    Action.SET_TEAM_SIGN -> {
                        BlockEntityTeamSign(b.fullChunk, BlockEntity.getDefaultCompound(b, BlockEntityTeamSign.NETWORK_ID))
                        actionPlayers.remove(p.uniqueId)
                        p.sendMessage(BedWars.prefix + TextFormat.GREEN + "Sign successfully set")
                    }
                }
            }
        }
    }

    enum class Action {
        SET_SIGN,
        SET_TEAM_SIGN
    }
}