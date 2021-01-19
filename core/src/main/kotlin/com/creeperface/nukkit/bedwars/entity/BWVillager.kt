package com.creeperface.nukkit.bedwars.entity


import cn.nukkit.entity.EntityCreature
import cn.nukkit.entity.passive.EntityVillagerV1
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.IntTag

class BWVillager(chunk: FullChunk, nbt: CompoundTag) : EntityCreature(chunk, nbt), NPC {

    val profession: String
        get() = this.namedTag.get("Profession").toString()

    val isBaby: Boolean
        get() = this.getDataFlag(14, 0)

    override fun getName() = "Villager"

    override fun getNetworkId() = NETWORK_ID

    override fun initEntity() {
        this.maxHealth = 20
        super.initEntity()
        if (!this.namedTag.contains("Profession")) {
            this.setProfession(1)
        }
    }

    override fun getWidth() = 0.4f

    override fun getHeight() = 1.7f

    fun setProfession(profession: Int) {
        this.namedTag.put("Profession", IntTag("Profession", profession))
    }

    companion object {

        const val NETWORK_ID = EntityVillagerV1.NETWORK_ID

        const val PROFESSION_FARMER = 0
        const val PROFESSION_LIBRARIAN = 1
        const val PROFESSION_PRIEST = 2
        const val PROFESSION_BLACKSMITH = 3
        const val PROFESSION_BUTCHER = 4
        const val PROFESSION_GENERIC = 5
    }


}