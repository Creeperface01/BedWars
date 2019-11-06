package com.creeperface.nukkit.bedwars.api.arena

import cn.nukkit.inventory.Inventory

interface Team {

    val enderChest: Inventory

    fun hasBed(): Boolean

    fun messagePlayers(message: String)

    fun getTeamPlayers(): Map<String, PlayerData>
}