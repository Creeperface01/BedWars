package com.creeperface.nukkit.bedwars.api.arena.configuration

import cn.nukkit.math.Vector3
import cn.nukkit.utils.DyeColor
import cn.nukkit.utils.TextFormat
import java.time.Instant

data class MapConfiguration(
        val name: String,
        val bronze: List<Vector3>,
        val iron: List<Vector3>,
        val gold: List<Vector3>,
        override val lastModification: Instant = Instant.now(),
        val teams: List<TeamData>
) : ModifiableConfiguration {

    interface ITeamData {
        val name: String
        val color: DyeColor
        val chatColor: TextFormat
        val spawn: Vector3
        val villager: Vector3
        val bed1: Vector3
        val bed2: Vector3
    }

    data class TeamData(
            override val name: String,
            override val color: DyeColor,
            override val chatColor: TextFormat,
            override val spawn: Vector3,
            override val villager: Vector3,
            override val bed1: Vector3,
            override val bed2: Vector3
    ) : ITeamData
}