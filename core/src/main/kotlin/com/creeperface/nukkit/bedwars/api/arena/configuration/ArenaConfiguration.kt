package com.creeperface.nukkit.bedwars.api.arena.configuration

import cn.nukkit.math.Vector3
import cn.nukkit.utils.DyeColor
import cn.nukkit.utils.TextFormat

class ArenaConfiguration(
        override val name: String,
        override val timeLimit: Int,
        override val startTime: Int,
        override val endingTime: Int,
        override val startPlayers: Int,
        override val bronzeDropInterval: Int,
        override val ironDropInterval: Int,
        override val goldDropInterval: Int,
        override val fastStartTime: Int,
        override val fastStartPlayers: Int,
        override val teamPlayers: Int,
        override val maxPlayers: Int,
        override val multiPlatform: Boolean,
        override val lobby: Vector3,
        override val teamData: List<IArenaConfiguration.TeamConfiguration>
) : IArenaConfiguration

interface IArenaConfiguration {
    val name: String
    val timeLimit: Int
    val startTime: Int
    val endingTime: Int
    val startPlayers: Int
    val bronzeDropInterval: Int
    val ironDropInterval: Int
    val goldDropInterval: Int
    val fastStartTime: Int
    val fastStartPlayers: Int
    val teamPlayers: Int
    val maxPlayers: Int
    val multiPlatform: Boolean
    val lobby: Vector3
    val teamData: List<TeamConfiguration>

    class TeamConfiguration(
            override val name: String,
            override val color: DyeColor,
            override val chatColor: TextFormat
    ) : ITeamConfiguration

    interface ITeamConfiguration {
        val name: String
        val color: DyeColor
        val chatColor: TextFormat
    }
}