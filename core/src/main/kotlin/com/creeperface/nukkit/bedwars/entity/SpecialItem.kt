package com.creeperface.nukkit.bedwars.entity

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.entity.item.EntityItem
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.TakeItemEntityPacket

class SpecialItem(chunk: FullChunk, nbt: CompoundTag) : EntityItem(chunk, nbt) {

    /*@Override
    public void initEntity() {
        super.initEntity();

        for (entity item : this.level.getNearbyEntities(new AxisAlignedBB(x - 2, y - 1, z - 2, x + 2, y + 1, z + 2), this)) {
            if (!(item instanceof EntityItem)) {
                continue;
            }

            EntityItem itemm = (EntityItem) item;

            if (!itemm.getItem().equals(this.item, true, false) || itemm.getItem().getCount() >= 64) {
                continue;
            }

            this.item.count += ((EntityItem) item).getItem().getCount();

            item.close();
        }
    }*/

    /*@Override
    public void spawnToAll() {
        entity[] entities = this.level.getNearbyEntities(new AxisAlignedBB(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1), this);

        for (entity entity : entities) {
            if (entity instanceof EntityItem) {
                return;
            }
        }

        super.spawnToAll();
    }

    @Override
    public void spawnTo(Player p) {
        entity[] entities = this.level.getNearbyEntities(new AxisAlignedBB(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1), this);

        for (entity entity : entities) {
            if (entity instanceof EntityItem) {
                return;
            }
        }

        super.spawnTo(p);
    }*/

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
