package com.creeperface.nukkit.bedwars.arena.manager

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityBed
import cn.nukkit.entity.Entity
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.arena.Team
import com.creeperface.nukkit.bedwars.entity.BWVillager
import com.creeperface.nukkit.bedwars.utils.plus
import com.creeperface.nukkit.bedwars.utils.ucFirst
import com.creeperface.nukkit.kformapi.KFormAPI
import com.creeperface.nukkit.kformapi.form.util.showForm

fun Arena.showTeamSelection(p: Player) {
    val form = KFormAPI.simpleForm {
        title(Lang.TEAM_SELECT.translate())

        teams.forEach { team ->
            val statusColor = if (team.canPlayerJoin(p)) TextFormat.GREEN else TextFormat.RED
            button(team.chatColor + team.name.ucFirst() + TextFormat.GRAY + " - " + statusColor + team.players.size + "/" + teamPlayers) {
                getPlayerData(p)?.let { data ->
                    addToTeam(p, team.id)
                }
            }
        }
    }

    p.showForm(form)
}

fun Team.canPlayerJoin(p: Player): Boolean {
    return !arena.isTeamFull(this) || arena.isTeamFree(this) || p.hasPermission("bedwars.joinfullteam")
}

fun Arena.filterAvailableMaps(): Collection<MapConfiguration> {
    val maps = plugin.maps.values
    if (!mapFilter.enable) {
        return maps
    }

    return maps.filter {
        if (mapFilter.teamCount.isNotEmpty() && it.teams.size !in mapFilter.teamCount) {
            return@filter false
        }

        if (mapFilter.include.isNotEmpty()) {
            return@filter it.name in mapFilter.include
        } else if (mapFilter.exclude.isNotEmpty()) {
            return@filter it.name !in mapFilter.exclude
        }

        return@filter true
    }
}

fun Arena.spawnBeds() {
    for (team in teams) {
        val positions = arrayOf(team.bed1, team.bed2)

        for (pos in positions) {
            val nbt = BlockEntity.getDefaultCompound(pos, BlockEntity.BED)
            nbt.putByte("color", team.color.woolData)

            BlockEntityBed(this.level.getChunk(pos.chunkX, pos.chunkZ), nbt)
        }
    }
}

fun Arena.spawnVillagers() {
    this.teams.forEach {
        val bb = SimpleAxisAlignedBB(
                it.villager.floor(),
                it.villager.floor().add(1.0, 1.0, 1.0)
        )

        val pos = it.villager.floor().add(0.5, 0.0, 0.5)
        val chunk = level.getChunk(pos.chunkX, pos.chunkZ)

        chunk.entities.values.forEach { ent ->
            if (bb.isVectorInside(ent)) {
                ent.close()
            }
        }

        BWVillager(
                chunk,
                Entity.getDefaultNBT(pos)
        )
    }
}