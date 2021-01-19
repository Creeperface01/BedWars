package com.creeperface.nukkit.bedwars.utils

import cn.nukkit.nbt.tag.*
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.treeToValue

enum class NbtType(val id: Byte, vararg aliases: String) {
    BYTE(Tag.TAG_Byte),
    SHORT(Tag.TAG_Short),
    INT(Tag.TAG_Int, "integer"),
    LONG(Tag.TAG_Long),
    FLOAT(Tag.TAG_Float),
    DOUBLE(Tag.TAG_Double),
    BYTE_ARRAY(Tag.TAG_Byte_Array),
    STRING(Tag.TAG_String, "text"),
    LIST(Tag.TAG_List),
    COMPOUND(Tag.TAG_Compound, "map", "object"),
    INT_ARRAY(Tag.TAG_Int_Array);

    val aliases = arrayOf(*aliases) + this.name.toLowerCase()

    companion object {

        private val mapping = mutableMapOf<String, NbtType>()
        private val idMapping = mutableMapOf<Byte, NbtType>()

        operator fun get(value: String) = mapping[value.toLowerCase()]

        operator fun get(value: Int) = idMapping[value.toByte()]

        operator fun get(value: Byte) = idMapping[value]

        init {
            values().forEach {
                it.aliases.forEach { alias ->
                    mapping[alias] = it
                }

                run {
                    idMapping[it.id] = it
                }
            }
        }
    }
}

fun Int.toIntTag(name: String? = null) = run {
    val tag = IntTag(name)
    tag.data = this

    tag
}

fun Int.toShortTag(name: String? = null) = run {
    val tag = ShortTag(name)
    tag.data = this

    tag
}

fun Int.toByteTag(name: String? = null) = run {
    val tag = ByteTag(name)
    tag.data = this

    tag
}

fun Long.toLongTag(name: String? = null) = run {
    val tag = LongTag(name)
    tag.data = this

    tag
}

fun Float.toFloatTag(name: String? = null) = run {
    val tag = FloatTag(name)
    tag.data = this

    tag
}

fun Double.toDoubleTag(name: String? = null) = run {
    val tag = DoubleTag(name)
    tag.data = this

    tag
}

fun <T : Tag> Iterable<T>.toListTag(): ListTag<T> {
    val tag = ListTag<T>()

    this.forEach { tag.add(it) }

    return tag
}

fun Iterable<Map.Entry<String, JsonNode>>.toCompoundTag(): CompoundTag {
    val tag = CompoundTag()

    this.forEach {
        tag.put(it.key, mapper.treeToValue(it.value))
    }

    return tag
}