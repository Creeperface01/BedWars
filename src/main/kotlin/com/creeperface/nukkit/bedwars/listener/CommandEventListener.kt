package com.creeperface.nukkit.bedwars.listener

import cn.nukkit.block.BlockSignPost
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.event.EventHandler
import cn.nukkit.event.EventPriority
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityArenaSign
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityTeamSign
import com.creeperface.nukkit.bedwars.utils.fullChunk
import com.creeperface.nukkit.bedwars.utils.register
import com.creeperface.nukkit.kformapi.KFormAPI
import com.creeperface.nukkit.kformapi.form.util.showForm
import java.util.*

class CommandEventListener(private val plugin: BedWars) : Listener {

    val actionPlayers = mutableMapOf<UUID, Action>()

    fun register() {
        register(plugin, this::onInteract, EventPriority.LOW, true)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onInteract(e: PlayerInteractEvent) {
        val p = e.player
        val b = e.block

        val action = actionPlayers[p.uniqueId] ?: return

        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            if (b is BlockSignPost) {
                when (action) {
                    Action.SET_SIGN -> {
                        plugin.formManager.createArenaSelection(p) { arena ->
                            if (b.level?.provider == null || b.level.getBlock(b) !is BlockSignPost) {
                                p.sendMessage(Lang.ERROR.translatePrefix())
                                return@createArenaSelection
                            }

                            BlockEntityArenaSign(
                                    b.fullChunk,
                                    BlockEntity.getDefaultCompound(b, BlockEntityArenaSign.NETWORK_ID)
                                            .putString("bw-arena", arena.name)
                            )
                            actionPlayers.remove(p.uniqueId)
                            p.sendMessage(BedWars.chatPrefix + TextFormat.GREEN + "Sign successfully set")
                        }
                    }
                    Action.SET_TEAM_SIGN -> {
                        plugin.formManager.createArenaSelection(p) { arena ->
                            val teamSelect = KFormAPI.customForm {
                                title("Team select")

//                                dropdown("Team", arena.tea)

                                input("Team ID", "0")

                                onSubmit { player, response ->
                                    if (b.level?.provider == null || b.level.getBlock(b) !is BlockSignPost) {
                                        p.sendMessage(Lang.ERROR.translatePrefix())
                                        return@onSubmit
                                    }

                                    val teamId = response.getInput(0).toIntOrNull()

                                    if (teamId == null) {
                                        p.sendMessage(BedWars.chatPrefix + TextFormat.RED + "Team ID must be an integer")
                                        return@onSubmit
                                    }

                                    BlockEntityTeamSign(
                                            b.fullChunk,
                                            BlockEntity.getDefaultCompound(b, BlockEntityTeamSign.NETWORK_ID)
                                                    .putString("bw-arena", arena.name)
                                                    .putInt("bw-team", teamId)
                                    )
                                    actionPlayers.remove(p.uniqueId)
                                    p.sendMessage(BedWars.chatPrefix + TextFormat.GREEN + "Sign successfully set")
                                }
                            }

                            p.showForm(teamSelect)
                        }
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