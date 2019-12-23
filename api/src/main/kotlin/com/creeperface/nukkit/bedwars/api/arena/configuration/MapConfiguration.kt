package com.creeperface.nukkit.bedwars.api.arena.configuration

import cn.nukkit.math.Vector3
import cn.nukkit.utils.DyeColor
import cn.nukkit.utils.TextFormat

class MapConfiguration(
        val name: String,
        val bronze: List<Vector3>,
        val iron: List<Vector3>,
        val gold: List<Vector3>,
        val teams: List<TeamData>
) {

    data class TeamData(
            val name: String,
            val color: DyeColor,
            val chatColor: TextFormat,
            val spawn: Vector3,
            val villager: Vector3,
            val bed1: Vector3,
            val bed2: Vector3
    )
}