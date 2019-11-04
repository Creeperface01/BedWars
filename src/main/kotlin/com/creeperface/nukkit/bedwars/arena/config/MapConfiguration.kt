package com.creeperface.nukkit.bedwars.arena.config

import cn.nukkit.math.Vector3

class MapConfiguration(
        val bronze: List<Vector3>,
        val iron: List<Vector3>,
        val gold: List<Vector3>,
        val teams: List<TeamData>
) {

    data class TeamData(
            val spawn: Vector3,
            val bed1: Vector3,
            val bed2: Vector3
    )
}