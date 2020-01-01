package com.creeperface.nukkit.bedwars.utils

import cn.nukkit.item.Item
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.math.Vector3
import cn.nukkit.utils.ConfigSection
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI
import com.creeperface.nukkit.placeholderapi.api.util.AnyContext
import kotlin.reflect.KClass

fun ConfigSection.getVector3(key: String? = null): Vector3 {
    val sec = if (key != null) this.getSection(key) else this

    val vector = Vector3()
    vector.x = sec.getDouble("x")
    vector.y = sec.getDouble("y")
    vector.z = sec.getDouble("z")

    return vector
}

@Suppress("UNCHECKED_CAST")
fun ConfigSection.getItem(key: String? = null, context: AnyContext): Item {
    val sec = if (key != null) this.getSection(key) else this
    val papi = PlaceholderAPI.getInstance()

    val item = Item.get(
            sec.getInt("item_id"),
            sec.getInt("item_damage"),
            sec.getInt("item_count")
    )

    if (sec.containsKey("item_custom_name")) {
        item.customName = papi.translateString(sec.getString("item_custom_name"), context = context)
    }

    if (sec.containsKey("lore")) {
        item.setLore(*sec.getStringList("lore").map { papi.translateString(it, context = context) }.toTypedArray())
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

        this@putItem[key] = this
    }
}

fun <T : Enum<T>> ConfigSection.getEnum(enumClass: KClass<T>, key: String): T {
    return java.lang.Enum.valueOf(enumClass.java, this[key].toString().toUpperCase())
}

fun <T : Enum<T>> ConfigSection.setEnum(key: String, value: T) {
    this[key] = value.name.toLowerCase()
}