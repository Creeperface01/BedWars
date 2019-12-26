package com.creeperface.nukkit.bedwars.utils

import cn.nukkit.item.Item
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.math.Vector3
import cn.nukkit.utils.ConfigSection

fun ConfigSection.getVector3(key: String): Vector3 {
    return this.getSection(key).getVector3()
}

fun ConfigSection.getVector3(): Vector3 {
    val vector = Vector3()
    vector.x = this.getDouble("x")
    vector.y = this.getDouble("y")
    vector.z = this.getDouble("z")

    return vector
}

@Suppress("UNCHECKED_CAST")
fun ConfigSection.getItem(key: String): Item {
    val sec = this.getSection(key)

    val item = Item.get(
            sec.getInt("item_id"),
            sec.getInt("item_damage"),
            sec.getInt("item_count")
    )

    if (sec.containsKey("item_custom_name")) {
        item.customName = sec.getString("item_custom_name")
    }

    if (sec.containsKey("lore")) {
        item.setLore(*sec.getStringList("lore").toTypedArray())
    }

    if (sec.containsKey("enchantments")) {
        val enchantments = sec.getList("enchantments") as List<ConfigSection>

        item.addEnchantment(
                *(enchantments.map {
                    Enchantment.getEnchantment(it.getInt("id")).setLevel(it.getInt("level"))
                }.toTypedArray())
        )
    }

    return item
}

fun ConfigSection.putItem(key: String, item: Item) {
    with(ConfigSection()) {
        this["item_id"] = item.id
        this["item_damage"] = item.damage
        this["item_count"] = item.count

        if (item.hasCustomName())
            this["item_custom_name"] = item.customName

        item.lore?.let { lore ->
            if (lore.isEmpty())
                return@let

            this["lore"] = lore
        }

        item.enchantments?.let { enchants ->
            if (enchants.isEmpty())
                return@let

            val ench = enchants.map {
                val sec = ConfigSection()

                sec["id"] = it.id
                sec["level"] = it.level

                sec
            }

            this["enchantments"] = ench
        }

        this["item_path"] = item.name
        this@putItem[key] = this
    }
}