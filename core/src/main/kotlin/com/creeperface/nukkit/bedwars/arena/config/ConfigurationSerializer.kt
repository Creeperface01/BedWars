package com.creeperface.nukkit.bedwars.arena.config

import com.creeperface.nukkit.bedwars.utils.ConfMap
import com.creeperface.nukkit.bedwars.utils.mapper
import com.fasterxml.jackson.module.kotlin.convertValue

/**
 * Created by CreeperFace on 2.7.2017.
 */
object ConfigurationSerializer {


    inline fun <reified T : Any> loadClass(cfg: ConfMap) = mapper.convertValue<T>(cfg)

}
