package com.creeperface.nukkit.bedwars.blockentity

import cn.nukkit.blockentity.BlockEntitySign
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.arena.Arena

class BlockEntityTeamSign(chunk: FullChunk, nbt: CompoundTag) : BlockEntitySign(chunk, nbt) {

    val team: Int
    private lateinit var arena: Arena

    private var lastSignUpdate = 0L

    init {
        if (!nbt.contains("bw-team")) {
            close()
            team = 0
        } else {
            team = nbt.getInt("bw-team")
            val arena = BedWars.instance.getArena(nbt.getString("bw-arena"))

            if (arena == null) {
                close()
            } else {
                this.arena = arena
            }

            scheduleUpdate()
        }
    }

    override fun onUpdate(): Boolean {
        val time = System.currentTimeMillis()

        if (arena.signManager.lastTeamSignsUpdate > lastSignUpdate) {
            updateData()
            lastSignUpdate = time
        }
        return true
    }

    private fun updateData() {
        this.setText(*arena.signManager.getData(this.team))
    }

    companion object {

        const val NETWORK_ID = "bw-team-sign"
    }
}