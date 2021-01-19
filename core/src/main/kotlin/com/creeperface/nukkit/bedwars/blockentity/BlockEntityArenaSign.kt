package com.creeperface.nukkit.bedwars.blockentity

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntitySign
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.arena.Arena

class BlockEntityArenaSign(chunk: FullChunk, nbt: CompoundTag) : BlockEntitySign(chunk, nbt) {

    val arena: Arena? by lazy {
        val arena = BedWars.instance.getArena(nbt.getString("bw-arena"))
//        logInfo("arena first load")

        if (arena != null) {
            scheduleUpdate()
        }

        return@lazy arena
    }

    private var lastSignUpdate = -1L

    private val initialized = true

    init {
//        logInfo("init")
        scheduleUpdate()
    }

    override fun onUpdate(): Boolean {
        return onUpdate(false)
    }

    fun onUpdate(force: Boolean): Boolean {
        val spawn = lastSignUpdate == -1L

        arena?.let {
            if (force || it.signManager.lastMainSignUpdate > lastSignUpdate) {
                updateData()
            }
        }

        if (spawn) {
//            logInfo("first update")
            spawnToAll()
        }

        return true
    }

    private fun updateData() {
        arena?.let {
//            logInfo("update data")
            lastSignUpdate = System.currentTimeMillis()
            this.setText(*it.signManager.mainSign)
        }
    }

    override fun spawnTo(player: Player?) {
        if (!initialized) { //hack
//            logInfo("spawn !initialized")
            return
        }

        if (lastSignUpdate == -1L) {
//            logInfo("spawn first update")
            arena?.let {
                onUpdate(false)
            }
        }

        super.spawnTo(player)
    }

    override fun getSpawnCompound(): CompoundTag {
        if (lastSignUpdate == -1L) {
            updateData()
        }
        return super.getSpawnCompound()
    }

    companion object {

        const val NETWORK_ID = "bw-sign"
    }
}