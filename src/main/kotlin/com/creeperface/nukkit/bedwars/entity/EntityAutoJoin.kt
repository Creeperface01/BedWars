package com.creeperface.nukkit.bedwars.entity

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.entity.passive.EntityVillager
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.AddEntityPacket
import cn.nukkit.network.protocol.MoveEntityAbsolutePacket

/**
 * @author CreeperFace
 */
class EntityAutoJoin(chunk: FullChunk, nbt: CompoundTag)//setNameTag(""+TextFormat.BOLD+TextFormat.GOLD+">> "+TextFormat.AQUA+"Click to join"+TextFormat.GOLD+" <<");
    : Entity(chunk, nbt) {

    override fun getNetworkId(): Int {
        return EntityVillager.NETWORK_ID
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        source.setCancelled()
        this.getServer().pluginManager.callEvent(source)
        return false
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (closed)
            return false

        val pos = this.add(0.0, 1.62)
        for (p in getLevel().players.values) {
            if (p.distanceSquared(this) < 25) {
                //MainLogger.getLogger().info("rotated");
                val diff = p.add(0.0, p.eyeHeight.toDouble()).subtract(pos).normalize()

                val DistanceXZ = Math.sqrt(diff.x * diff.x + diff.z * diff.z)
                val DistanceY = Math.sqrt(DistanceXZ * DistanceXZ + diff.y * diff.y)
                var newYaw = Math.acos(diff.x / DistanceXZ) * 180 / Math.PI
                val newPitch = Math.acos(diff.y / DistanceY) * 180 / Math.PI - 90
                if (diff.z < 0.0)
                    newYaw = newYaw + Math.abs(180 - newYaw) * 2
                newYaw = newYaw - 90

                val pk = MoveEntityAbsolutePacket()
                pk.eid = this.getId()
                pk.x = this.x
                pk.y = this.y
                pk.z = this.z
                pk.headYaw = newYaw
                pk.yaw = newYaw
                pk.pitch = newPitch

                p.dataPacket(pk)
            }
        }

        super.onUpdate(currentTick)
        return true
    }

    override fun spawnTo(player: Player) {
        val pk = AddEntityPacket()
        pk.type = this.networkId
        pk.entityUniqueId = this.getId()
        pk.entityRuntimeId = this.getId()
        pk.x = this.x.toFloat()
        pk.y = this.y.toFloat()
        pk.z = this.z.toFloat()
        pk.speedX = this.motionX.toFloat()
        pk.speedY = this.motionY.toFloat()
        pk.speedZ = this.motionZ.toFloat()
        pk.metadata = this.dataProperties
        player.dataPacket(pk)
        super.spawnTo(player)
    }
}
