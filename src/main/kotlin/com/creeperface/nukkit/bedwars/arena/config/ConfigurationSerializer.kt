package com.creeperface.nukkit.bedwars.arena.config

import cn.nukkit.item.Item
import cn.nukkit.math.Vector3
import com.creeperface.nukkit.bedwars.api.arena.configuration.IArenaConfiguration
import com.creeperface.nukkit.bedwars.api.utils.InventoryItem
import com.creeperface.nukkit.bedwars.utils.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.typeOf

/**
 * Created by CreeperFace on 2.7.2017.
 */
@OptIn(ExperimentalStdlibApi::class)
object ConfigurationSerializer {

    private val typeReaders = mutableMapOf<KType, (ConfMap, String) -> Any?>()

    init {
        typeReaders[typeOf<Vector3>()] = { section, key -> section.readVector3(key) }
        typeReaders[typeOf<Item>()] = { section, key -> section.readItem(key) }
        typeReaders[typeOf<InventoryItem>()] = { section, key -> section.readInventoryItem(key) }
    }

    fun <T : Any> loadClass(cfg: ConfMap, clazz: KClass<T>) = cfg.readClass(clazz)

    private fun <T : Any> ConfMap.readClass(clazz: KClass<T>): T {
        val constructors = clazz.constructors.toMutableList()

        clazz.primaryConstructor?.let {
            constructors.add(0, it)
        }

        val params = mutableMapOf<KParameter, Any?>()

        constructors.forEach const@{ constructor ->
            constructor.parameters.forEach param@{ param ->
                typeReaders[param.type]?.let {
                    params[param] = it(this, param.name!!)
                    return@param
                }

                val snakeCase = param.name?.camelToSnakeCase() ?: error("Could not obtain parameter name")

                if (this.contains(snakeCase)) {
                    params[param] = this[snakeCase]
                } else if (!param.isOptional) {
//                    throw RuntimeException("Parameter ${param.name} is missing in the configuration")
                    return@const
                }
            }

            return constructor.callBy(params)
        }

        throw RuntimeException("Callable constructor not found")
    }
}
