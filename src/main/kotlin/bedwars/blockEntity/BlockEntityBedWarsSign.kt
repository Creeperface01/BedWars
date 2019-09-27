package bedwars.blockEntity

import cn.nukkit.blockentity.BlockEntitySign
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

class BlockEntityBedWarsSign(chunk: FullChunk, nbt: CompoundTag) : BlockEntitySign(chunk, nbt) {

    private val signType: SignType
    private val data: Map<String, Any>

    init {
        val type = nbt.getString("bwSignType")
        signType = SignType.valueOf(type)
        data = nbt.getCompound("data").tags
    }

//    private var lastSignUpdate = 0L

//    override fun onUpdate(): Boolean {
//        val time = System.currentTimeMillis()
//
//        if(time - lastSignUpdate > 1000) {
//            lastSignUpdate = time
//
//        }
//        return true
//    }

    override fun close() {
        super.close()
    }

    private fun updateData() {

    }

    enum class SignType {
        ARENA,
        TEAM
    }
}