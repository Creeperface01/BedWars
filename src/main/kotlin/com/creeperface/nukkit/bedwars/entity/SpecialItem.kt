package com.creeperface.nukkit.bedwars.entity

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.entity.item.EntityItem
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.TakeItemEntityPacket

class SpecialItem(chunk: FullChunk, nbt: CompoundTag) : EntityItem(chunk, nbt) {

    override fun onUpdate(diff: Int): Boolean {
        var result = super.onUpdate(diff)

        if (!closed && isAlive) {
            val entities = this.level.getNearbyEntities(this.boundingBox.grow(1.0, 1.0, 1.0), this)

            for (entity in entities) {
                if (entity is Player) {

                    if (entity.gamemode > 1) {
                        continue
                    }

                    var pk = TakeItemEntityPacket()
                    pk.entityId = entity.id
                    pk.target = this.getId()

                    Server.broadcastPacket(entity.getViewers().values, pk)

                    pk = TakeItemEntityPacket()
                    pk.entityId = 0L
                    pk.target = this.getId()
                    entity.dataPacket(pk)
                    entity.inventory.addItem(this.item.clone())
                    result = false
                    this.close()
                    break
                }
            }
        }

        return result
    }
}
