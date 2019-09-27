package bedwars.shop

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.block.BlockAir
import cn.nukkit.inventory.BaseInventory
import cn.nukkit.inventory.InventoryType
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.GlobalBlockPalette
import cn.nukkit.math.BlockVector3
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.*
import java.io.IOException
import java.nio.ByteOrder
import java.util.*

open class ShopInventory : BaseInventory(FakeHolder(), InventoryType.CHEST) {

    private val spawnedBlocks = HashMap<String, BlockVector3>()

    var pos = BlockVector3()

    override fun getHolder(): FakeHolder {
        return this.holder as FakeHolder
    }

    override fun onOpen(who: Player) { //method called when player opens an inventory
        super.onOpen(who)

        pos = BlockVector3(who.floorX, who.floorY + 3, who.floorZ)

        val updateBlockPacket = UpdateBlockPacket()
        updateBlockPacket.x = pos.x
        updateBlockPacket.y = pos.y
        updateBlockPacket.z = pos.z
        updateBlockPacket.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(Block.CHEST, 0)
        updateBlockPacket.flags = UpdateBlockPacket.FLAG_NONE


        spawnedBlocks[who.name.toLowerCase()] = pos.clone()
        who.dataPacket(updateBlockPacket)

        val bep = BlockEntityDataPacket()
        bep.x = pos.x
        bep.y = pos.y
        bep.z = pos.z

        try {
            bep.namedTag = NBTIO.write(getSpawnCompound(pos), ByteOrder.LITTLE_ENDIAN)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        who.dataPacket(bep)

        val pk = ContainerOpenPacket()
        pk.windowId = who.getWindowId(this).toByte().toInt()
        pk.type = this.getType().networkType.toByte().toInt()
        //pk.entityId = who.getId();
        //MainLogger.getLogger().info("opening ID: "+pk.windowId);

        pk.x = pos.x
        pk.y = pos.y
        pk.z = pos.z

        who.dataPacket(pk)

        this.sendContents(who)
    }

    override fun onClose(who: Player) { //method called when player closes an inventory
        val pk2 = ContainerClosePacket()
        pk2.windowId = who.getWindowId(this).toByte().toInt()
        who.dataPacket(pk2)

        val v = spawnedBlocks[who.name.toLowerCase()]

        if (v != null && who.getLevel().isChunkLoaded(v.x shr 4, v.z shr 4)) {
            who.getLevel().sendBlocks(arrayOf(who), arrayOf(Vector3(v.x.toDouble(), v.y.toDouble(), v.z.toDouble())))
        }

        spawnedBlocks.remove(who.name.toLowerCase())

        super.onClose(who)
    }

    private fun getSpawnCompound(v: BlockVector3): CompoundTag {
        val c = CompoundTag().putString("id", "Chest").putInt("x", v.x).putInt("y", v.y).putInt("z", v.z)
        c.putString("CustomName", "shop") //name of the inventory

        return c
    }

    override fun sendContents(p: Player) { //this method is useful if you open inventory without closing a previous one, it cleans all slots and replace them with new items from the inventory
        val id = p.getWindowId(this)

        //MainLogger.getLogger().info("send empty slots  "+id);
        val pk = InventoryContentPacket()
        pk.slots = arrayOfNulls(InventoryType.CHEST.defaultSize)
        pk.inventoryId = id

        run {
            var i = 0
            while (i < getSize() && i < pk.slots.size) {
                pk.slots[i] = getItem(i)
                i++
            }
        }

        val air = ItemBlock(BlockAir())
        for (i in getSize() until pk.slots.size) {
            pk.slots[i] = air
        }

        p.dataPacket(pk)

        //MainLogger.getLogger().info("send real slots");
        //super.sendContents(p);
    }
}
