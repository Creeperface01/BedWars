package com.creeperface.nukkit.bedwars.entity

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.ByteEntityData
import cn.nukkit.entity.passive.EntitySheep
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import com.creeperface.nukkit.bedwars.api.utils.BedWarsExplosion
import com.creeperface.nukkit.bedwars.arena.Team
import com.creeperface.nukkit.bedwars.arena.handler.ArenaGame
import me.onebone.actaeon.entity.MovingEntity
import me.onebone.actaeon.target.EntityTarget

class TNTShip(chunk: FullChunk, nbt: CompoundTag, private val arena: ArenaGame, private val team: Team) :
    MovingEntity(chunk, nbt) {

    init {
        this.setDataProperty(ByteEntityData(Entity.DATA_COLOUR, team.color.woolData))
        setMovementSpeed(0.29f)
    }

    override fun getNetworkId(): Int {
        return NETWORK_ID
    }

    override fun getWidth(): Float {
        return 0.9f
    }

    override fun getHeight(): Float {
        return 1.3f
    }

    public override fun initEntity() {
        super.initEntity()
        this.maxHealth = 8
    }

    override fun entityBaseTick(tickDiff: Int): Boolean {
        val hasUpdate = super.entityBaseTick(tickDiff)

        if (this.ticksLived > EXPLODE_TIME * 20) {
            explode()
            return false
        }

        if (!route.isSearching && (this.realTarget == null || this.target.distanceSquared(this.realTarget) >= TARGET_DISTANCE)) {
            var target: Player? = null
            var dist = TARGET_DISTANCE
            var team: Team?

            for (p in this.level.players.values) {
                val distance = p.distanceSquared(this)

                if (distance > dist) continue

                team = arena.getPlayerTeam(p)
                if (team != null && team.id != this.team.id) {
                    target = p
                    dist = distance
                }
            }

            if (target != null) {
                this.setTarget(EntityTarget.builder().target(target).identifier("target").build())
            }
        }

        return hasUpdate
    }

    private fun explode() {
        val explosion = BedWarsExplosion(this, 6.0, this)
        explosion.explode(arena, team.id)

        this.close()
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        return false
    }

    override fun getRange(): Double {
        return 0.5
    }

    companion object {

        const val NETWORK_ID = EntitySheep.NETWORK_ID
        const val TARGET_DISTANCE = (20 * 20).toDouble()
        const val EXPLODE_TIME = 20
    }
}
