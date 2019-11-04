package com.creeperface.nukkit.bedwars.blockentity

import cn.nukkit.blockentity.BlockEntitySign
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.arena.Arena

class BlockEntityTeamSign(chunk: FullChunk, nbt: CompoundTag) : BlockEntitySign(chunk, nbt) {

    val team: Int = nbt.getInt("bw-team")
    private lateinit var arena: Arena

    init {
        val arena = BedWars.instance.getArena(nbt.getString("bw-arena"))

        if (arena == null) {
            close()
        } else {
            this.arena = arena
        }
    }

    private var lastSignUpdate = 0L

    override fun onUpdate(): Boolean {
        val time = System.currentTimeMillis()

        if (time - lastSignUpdate > 1000) {
            lastSignUpdate = time

        }
        return true
    }

    private fun updateData() {

    }

    enum class SignType {
        ARENA,
        TEAM
    }
}