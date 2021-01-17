package com.creeperface.nukkit.bedwars.api.arena.configuration

import cn.nukkit.math.Vector3
import com.creeperface.nukkit.bedwars.api.utils.InventoryItem
import com.creeperface.nukkit.bedwars.api.utils.watch
import com.fasterxml.jackson.annotation.JacksonInject
import com.fasterxml.jackson.annotation.JsonProperty

class ArenaConfiguration(
    @JacksonInject conf: MutableConfiguration,

    override val name: String,
    override val lobby: Vector3? = null,

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
    override val teamSelectItem: InventoryItem? = null,
    @JsonProperty("voting") override val voteConfig: VoteConfig,
    override val lobbyItem: InventoryItem?,
    override val mapFilter: MapFilter
) : IArenaConfiguration, MutableConfiguration by conf

interface IArenaConfiguration : MutableConfiguration {
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
    val lobby: Vector3?
    val teamSelectCommand: Boolean
    val teamSelectItem: InventoryItem?
    val voteConfig: VoteConfig
    val lobbyItem: InventoryItem?
    val mapFilter: MapFilter
}

class VoteConfig(
    @JacksonInject parent: MutableConfiguration,
    enable: Boolean,
    maxOptions: Int,
    players: Int,
    countdown: Int,
    item: InventoryItem?
) {

    val enable: Boolean by watch(parent, enable)
    val maxOptions: Int by watch(parent, maxOptions)
    val players: Int by watch(parent, players)
    val countdown: Int by watch(parent, countdown)
    val item: InventoryItem? by watch(parent, item)
}

class MapFilter(
    @JacksonInject parent: MutableConfiguration,
    enable: Boolean,
    teamCount: Set<Int> = emptySet(),
    include: List<String> = emptyList(),
    exclude: List<String> = emptyList()
) {
    val enable: Boolean by watch(parent, enable)
    val teamCount: Set<Int> by watch(parent, teamCount)
    val include: List<String> by watch(parent, include)
    val exclude: List<String> by watch(parent, exclude)
}