package com.creeperface.nukkit.bedwars.shop.inventory

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.block.BlockAir
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.inventory.BaseInventory
import cn.nukkit.inventory.InventoryType
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.GlobalBlockPalette
import cn.nukkit.math.BlockVector3
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.*
import com.creeperface.nukkit.bedwars.utils.logError
import java.io.IOException
import java.nio.ByteOrder
import java.util.*

open class ShopInventory : BaseInventory(FakeHolder(), InventoryType.CHEST) {

    private val spawnedBlocks = HashMap<Long, BlockVector3>()

    override fun getHolder() = this.holder as FakeHolder

    override fun onOpen(who: Player) { //method called when player opens an inventory
        super.onOpen(who)

        val pos = BlockVector3(who.floorX, who.floorY + 3, who.floorZ)

        val updateBlockPacket = UpdateBlockPacket()
        updateBlockPacket.x = pos.x
        updateBlockPacket.y = pos.y
        updateBlockPacket.z = pos.z
        updateBlockPacket.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(Block.CHEST, 0)
        updateBlockPacket.flags = UpdateBlockPacket.FLAG_ALL_PRIORITY

        spawnedBlocks[who.id] = pos.clone()
        who.dataPacket(updateBlockPacket)

        val bep = BlockEntityDataPacket()
        bep.x = pos.x
        bep.y = pos.y
        bep.z = pos.z

        try {
            bep.namedTag = NBTIO.write(getSpawnCompound(pos), ByteOrder.LITTLE_ENDIAN, true)
        } catch (e: IOException) {
            logError("Error while writing nametag", e)
        }

        who.dataPacket(bep)

        val pk = ContainerOpenPacket()
        pk.windowId = who.getWindowId(this)
        pk.type = this.getType().networkType

        pk.x = pos.x
        pk.y = pos.y
        pk.z = pos.z

        who.dataPacket(pk)

        this.sendContents(who)
    }

    override fun onClose(who: Player) { //method called when player closes an inventory
        val pk = ContainerClosePacket()
        pk.windowId = who.getWindowId(this)
        pk.wasServerInitiated = who.closingWindowId != pk.windowId
        who.dataPacket(pk)

        val v = spawnedBlocks[who.id]

        if (v != null && who.getLevel().isChunkLoaded(v.x shr 4, v.z shr 4)) {
            who.getLevel().sendBlocks(
                arrayOf(who),
                arrayOf(Vector3(v.x.toDouble(), v.y.toDouble(), v.z.toDouble())),
                UpdateBlockPacket.FLAG_ALL_PRIORITY
            )
        }

        spawnedBlocks.remove(who.id)

        super.onClose(who)
    }

    private fun getSpawnCompound(v: BlockVector3): CompoundTag {
        val c = CompoundTag().putString("id", BlockEntity.CHEST).putInt("x", v.x).putInt("y", v.y).putInt("z", v.z)
        c.putString("CustomName", "Shop") //name of the inventory

        return c
    }

    override fun sendContents(p: Player) { //this method is useful if you open inventory without closing a previous one, it cleans all slots and replace them with new items from the inventory
        val id = p.getWindowId(this)

        //MainLogger.getLogger().info("send empty slots  "+id);
        val pk = InventoryContentPacket()
        pk.inventoryId = id

        val air = ItemBlock(BlockAir())
        pk.slots = Array(InventoryType.CHEST.defaultSize) {
            if (it < getSize()) {
                getItem(it)
            } else {
                air
            }
        }

        p.dataPacket(pk)

        //MainLogger.getLogger().info("send real slots");
        //super.sendContents(p);
    }
}
