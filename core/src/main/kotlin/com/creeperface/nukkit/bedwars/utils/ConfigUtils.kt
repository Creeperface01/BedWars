package com.creeperface.nukkit.bedwars.utils

import cn.nukkit.item.Item
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.math.Vector3
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI
import com.creeperface.nukkit.placeholderapi.api.util.AnyContext
import kotlin.reflect.KClass

typealias ConfMap = Map<String, *>
typealias MutableConfMap = MutableMap<String, in Any?>
typealias InsConfMap = HashMap<String, Any?>

@Suppress("UNCHECKED_CAST")
fun <T> ConfMap.read(key: String, defaultValue: T) = this[key] as? T ?: defaultValue

fun ConfMap.readInt(key: String, defaultValue: Int = 0) = read(key, defaultValue)

fun ConfMap.readDouble(key: String, defaultValue: Double = 0.0) = read(key, defaultValue)

fun ConfMap.readLong(key: String, defaultValue: Long = 0) = read(key, defaultValue)

fun ConfMap.readString(key: String, defaultValue: String = "") = read(key, defaultValue)

fun ConfMap.readBoolean(key: String, defaultValue: Boolean = false) = read(key, defaultValue)

fun ConfMap.readSection(key: String, defaultValue: ConfMap = mutableMapOf<String, Any?>()) = read(key, defaultValue)

fun ConfMap.readIntList(key: String, defaultValue: List<Int> = emptyList()) = read(key, defaultValue)

fun ConfMap.readDoubleList(key: String, defaultValue: List<Double> = emptyList()) = read(key, defaultValue)

fun ConfMap.readLongList(key: String, defaultValue: List<Long> = emptyList()) = read(key, defaultValue)

fun ConfMap.readStringList(key: String, defaultValue: List<String> = emptyList()) = read(key, defaultValue)

fun ConfMap.readMapList(key: String, defaultValue: List<ConfMap> = emptyList()) = read(key, defaultValue)

fun ConfMap.readBooleanList(key: String, defaultValue: List<Boolean> = emptyList()) = read(key, defaultValue)

fun ConfMap.readCharList(key: String, defaultValue: List<Char> = emptyList()) = read(key, defaultValue)

fun ConfMap.readList(key: String, defaultValue: List<*> = emptyList<Any?>()) = read(key, defaultValue)

fun ConfMap.readVector3(key: String? = null): Vector3 {
    val sec = if (key != null) this.readSection(key) else this

    val vector = Vector3()
    vector.x = sec.readDouble("x")
    vector.y = sec.readDouble("y")
    vector.z = sec.readDouble("z")

    return vector
}

fun MutableConfMap.writeVector3(key: String, value: Vector3) {
    val confMap = InsConfMap()
    confMap["x"] = value.x
    confMap["y"] = value.y
    confMap["z"] = value.z

    this[key] = confMap
}

@Suppress("UNCHECKED_CAST")
fun ConfMap.readItem(key: String? = null, context: AnyContext): Item {
    val sec = if (key != null) this.readSection(key) else this
    val papi = PlaceholderAPI.getInstance()

    val item = Item.get(
            sec.readInt("item_id"),
            sec.readInt("item_damage"),
            sec.readInt("item_count")
    )

    if (sec.containsKey("item_custom_name")) {
        item.customName = papi.translateString(sec.readString("item_custom_name"), context = context)
    }

    if (sec.containsKey("lore")) {
        item.setLore(*sec.readStringList("lore").map { papi.translateString(it, context = context) }.toTypedArray())
    }

    if (sec.containsKey("enchantments")) {
        val enchantments = sec.readList("enchantments") as List<ConfMap>

        item.addEnchantment(
                *(enchantments.map {
                    Enchantment.getEnchantment(it.readInt("id")).setLevel(it.readInt("level"))
                }.toTypedArray())
        )
    }

    return item
}

fun MutableConfMap.writeItem(key: String, item: Item) {
    with(InsConfMap()) {
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
                val sec = InsConfMap()

                sec["id"] = it.id
                sec["level"] = it.level

                sec
            }

            this["enchantments"] = ench
        }

        this@writeItem[key] = this
    }
}

fun <T : Enum<T>> ConfMap.readEnum(enumClass: KClass<T>, key: String): T {
    return java.lang.Enum.valueOf(enumClass.java, this[key].toString().toUpperCase())
}

fun <T : Enum<T>> MutableConfMap.writeEnum(key: String, value: T) {
    this[key] = value.name.toLowerCase()
}