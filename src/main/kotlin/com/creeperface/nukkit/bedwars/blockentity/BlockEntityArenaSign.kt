package com.creeperface.nukkit.bedwars.blockentity

import cn.nukkit.blockentity.BlockEntitySign
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.utils.logInfo

class BlockEntityArenaSign(chunk: FullChunk, nbt: CompoundTag) : BlockEntitySign(chunk, nbt) {

    lateinit var arena: Arena
        private set

    private var lastSignUpdate = 0L

    init {
        val arena = BedWars.instance.getArena(nbt.getString("bw-arena"))

        if (arena == null) {
            close()
            logInfo("close arena sign")
        } else {
            this.arena = arena
            scheduleUpdate()
        }
    }

    override fun onUpdate(): Boolean {
        val time = System.currentTimeMillis()

        if (arena.signManager.lastMainSignUpdate > lastSignUpdate) {
            updateData()
            lastSignUpdate = time
        }
        return true
    }

    private fun updateData() {
        this.setText(*arena.signManager.mainSign)
    }

    companion object {

        const val NETWORK_ID = "bw-sign"
    }
}