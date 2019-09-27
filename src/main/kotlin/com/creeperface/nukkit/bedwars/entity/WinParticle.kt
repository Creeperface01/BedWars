package com.creeperface.nukkit.bedwars.entity

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.particle.DustParticle
import cn.nukkit.level.particle.InstantSpellParticle
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import java.util.*

class WinParticle(chunk: FullChunk, nbt: CompoundTag) : Entity(chunk, nbt) {

    private var startPos: Vector3? = null

    override fun getNetworkId(): Int {
        return -10
    }

    override fun initEntity() {
        super.initEntity()
        startPos = Vector3()
        startPos!!.setComponents(x, y, z)
    }

    override fun onUpdate(diff: Int): Boolean {
        val tick = getServer().tick

        val rnd = Random()

        this.level.addParticle(DustParticle(this, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)))

        if (this.y > startPos!!.y + 13) {
            this.level.addParticle(InstantSpellParticle(this, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)))
            this.level.addParticle(InstantSpellParticle(this, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)))
            close()
            return false
        }

        this.y += 0.3
        lastUpdate = tick

        return true
    }

    override fun spawnTo(player: Player) {

    }

    override fun despawnFrom(player: Player) {

    }
}
