package com.creeperface.nukkit.bedwars.arena.config

import cn.nukkit.math.Vector3
import com.creeperface.nukkit.bedwars.api.arena.configuration.IArenaConfiguration
import com.creeperface.nukkit.bedwars.utils.ConfMap
import com.creeperface.nukkit.bedwars.utils.readMapList
import com.creeperface.nukkit.bedwars.utils.readVector3
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
        typeReaders[typeOf<List<*>>()] = { section, key -> section.readTeams(key) }
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

                if (this.contains(param.name)) {
                    params[param] = this[param.name]
                } else if (!param.isOptional) {
//                    throw RuntimeException("Parameter ${param.name} is missing in the configuration")
                    return@const
                }
            }

            return constructor.callBy(params)
        }

        throw RuntimeException("Callable constructor not found")
    }

    private fun ConfMap.readTeams(key: String): List<IArenaConfiguration.TeamConfiguration> {
        val teams = mutableListOf<IArenaConfiguration.TeamConfiguration>()

        this.readMapList(key).forEach {
            teams.add(it.readClass(IArenaConfiguration.TeamConfiguration::class))
            return@forEach
        }

        return teams
    }

//    fun serialize(conf: ArenaConfiguration): String {
//        return ObjectMapper().writeValueAsString(conf)
//    }
//
//    fun serialize(conf: MapConfiguration): String {
//        return ObjectMapper().writeValueAsString(conf)
//    }
}
