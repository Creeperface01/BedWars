package bedwars.blockEntity

import cn.nukkit.block.Block
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Created by CreeperFace on 28.6.2017.
 */
class BlockEntityMine(chunk: FullChunk, nbt: CompoundTag) : BlockEntity(chunk, nbt) {

    private var team = -1

    override fun isBlockEntityValid(): Boolean {
        return this.getLevel().getBlockIdAt(this.floorX, this.floorY, this.floorZ) == Block.STONE_PRESSURE_PLATE
    }

    fun getTeam(): Int {
        if (this.team == -1) {
            this.team = this.namedTag.getInt("team")
        }

        return this.team
    }
}
