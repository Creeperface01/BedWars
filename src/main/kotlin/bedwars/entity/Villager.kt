package bedwars.entity


import cn.nukkit.Player
import cn.nukkit.entity.EntityCreature
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.IntTag
import cn.nukkit.network.protocol.AddEntityPacket

class Villager(chunk: FullChunk, nbt: CompoundTag) : EntityCreature(chunk, nbt), NPC {

    val profession: String
        get() = this.namedTag.get("Profession").toString()

    val isBaby: Boolean
        get() = this.getDataFlag(14, 0)

    override fun getName(): String {
        return "Villager"
    }

    override fun getNetworkId(): Int {
        return NETWORK_ID
    }

    override fun initEntity() {
        this.maxHealth = 20
        super.initEntity()
        if (!this.namedTag.contains("Profession")) {
            this.setProfession(1)
        }
    }

    override fun spawnTo(player: Player) {
        val pk = AddEntityPacket()
        pk.type = this.networkId
        pk.entityRuntimeId = this.getId()
        pk.entityUniqueId = this.getId()
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

    fun setProfession(profession: Int) {
        this.namedTag.put("Profession", IntTag("Profession", profession))
    }

    companion object {

        val NETWORK_ID = 15
        val PROFESSION_FARMER = 0
        val PROFESSION_LIBRARIAN = 1
        val PROFESSION_PRIEST = 2
        val PROFESSION_BLACKSMITH = 3
        val PROFESSION_BUTCHER = 4
        val PROFESSION_GENERIC = 5
    }


}