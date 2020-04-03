package com.creeperface.nukkit.bedwars.api.arena.configuration

import cn.nukkit.math.Vector3
import com.creeperface.nukkit.bedwars.api.utils.InventoryItem
import java.time.Instant

data class ArenaConfiguration(
        override val name: String,
        override val lobby: Vector3,
        override val lastModification: Instant = Instant.now(),

        override val timeLimit: Int,
        override val startTime: Int,
        override val endingTime: Int,
        override val startPlayers: Int,
        override val bronzeDropInterval: Int,
        override val ironDropInterval: Int,
        override val goldDropInterval: Int,
        override val fastStart: Boolean,
        override val fastStartTime: Int,
        override val fastStartPlayers: Int,
        override val teamPlayers: Int,
        override val maxPlayers: Int,
        override val multiPlatform: Boolean,
        override val teamSelectCommand: Boolean,
        override val teamSelectItem: InventoryItem?,
        override val voteItem: InventoryItem?,
        override val votePlayers: Int,
        override val voteCountdown: Int,
        override val mapFilter: MapFilter
) : IArenaConfiguration

interface IArenaConfiguration : ModifiableConfiguration {
    val name: String
    val timeLimit: Int
    val startTime: Int
    val endingTime: Int
    val startPlayers: Int
    val bronzeDropInterval: Int
    val ironDropInterval: Int
    val goldDropInterval: Int
    val fastStart: Boolean
    val fastStartTime: Int
    val fastStartPlayers: Int
    val teamPlayers: Int
    val maxPlayers: Int
    val multiPlatform: Boolean
    val lobby: Vector3
    val teamSelectCommand: Boolean
    val teamSelectItem: InventoryItem?
    val voteItem: InventoryItem?
    val votePlayers: Int
    val voteCountdown: Int
    val mapFilter: MapFilter
}

data class MapFilter(
        val enable: Boolean,
        val teamCount: Set<Int> = emptySet(),
        val include: List<String> = emptyList(),
        val exclude: List<String> = emptyList()
)