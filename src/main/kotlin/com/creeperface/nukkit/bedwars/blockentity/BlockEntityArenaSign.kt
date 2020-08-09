package com.creeperface.nukkit.bedwars.blockentity

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntitySign
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.arena.Arena

class BlockEntityArenaSign(chunk: FullChunk, nbt: CompoundTag) : BlockEntitySign(chunk, nbt) {

    lateinit var arena: Arena
        private set

    private var lastSignUpdate = 0L

    init {
        val arena = BedWars.instance.getArena(nbt.getString("bw-arena"))

        if (arena != null) {
            this.arena = arena
            scheduleUpdate()
        }
    }

    override fun onUpdate(): Boolean {
        return onUpdate(false)
    }

    fun onUpdate(force: Boolean): Boolean {
        val time = System.currentTimeMillis()

        if (force || arena.signManager.lastMainSignUpdate > lastSignUpdate) {
            updateData()
            lastSignUpdate = time
        }
        return true
    }

    private fun updateData() {
        this.setText(*arena.signManager.mainSign)
    }

    override fun spawnTo(player: Player?) {
        if (!::arena.isInitialized) {
            val arena = BedWars.instance.getArena(namedTag.getString("bw-arena"))

            if (arena != null) {
                this.arena = arena
                scheduleUpdate()
            } else {
                close()
                return
            }

            onUpdate(true)
        }

        super.spawnTo(player)
    }

    companion object {

        const val NETWORK_ID = "bw-sign"
    }
}