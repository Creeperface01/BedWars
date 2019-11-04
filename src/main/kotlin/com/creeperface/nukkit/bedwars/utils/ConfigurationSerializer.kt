package com.creeperface.nukkit.bedwars.utils

import cn.nukkit.math.Vector3
import cn.nukkit.utils.ConfigSection
import com.creeperface.nukkit.bedwars.arena.config.IArenaConfiguration
import com.creeperface.nukkit.bedwars.obj.Team
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.primaryConstructor

/**
 * Created by CreeperFace on 2.7.2017.
 */
object ConfigurationSerializer {

    private val typeReaders = mutableMapOf<KType, (ConfigSection, String) -> Any?>()

    init {
        typeReaders[Vector3::class.createType()] = { section, key -> section.readVector3(key) }
        typeReaders[List::class.createType(
                listOf(
                        KTypeProjection.invariant(Team::class.createType())
                )
        )] = { section, key -> section.readTeams(key) }
    }

    fun <T : Any> loadClass(cfg: ConfigSection, clazz: KClass<T>) = cfg.readClass(clazz)

    private fun <T : Any> ConfigSection.readClass(clazz: KClass<T>): T {
        val constructor = clazz.primaryConstructor!!
        val params = mutableMapOf<KParameter, Any?>()

        constructor.parameters.forEach { param ->
            typeReaders[param.type]?.let {
                params[param] = it(this, param.name!!)
                return@forEach
            }

            params[param] = this[param.name]
        }

        return constructor.callBy(params)
    }

    private fun ConfigSection.readVector3(key: String): Vector3 {
        val section1 = this.getSection(key)

        val vector = Vector3()
        vector.x = section1.getDouble("x")
        vector.y = section1.getDouble("y")
        vector.z = section1.getDouble("z")

        return vector
    }

    private fun ConfigSection.readTeams(key: String): List<IArenaConfiguration.TeamConfiguration> {
        val teams = mutableListOf<IArenaConfiguration.TeamConfiguration>()

        this.getList(key).forEach {
            require(it is ConfigSection) { "Invalid arena configuration file" }

            teams.add(it.readClass(IArenaConfiguration.TeamConfiguration::class))
        }

        return teams
    }

    fun offsetMap(map: HashMap<String, Vector3>, x: Double, z: Double) {
        for ((key, v) in map) {
            if (key == "sign") continue

            v.setComponents(v.x + x, v.y, v.z + z)
        }
    }
}
