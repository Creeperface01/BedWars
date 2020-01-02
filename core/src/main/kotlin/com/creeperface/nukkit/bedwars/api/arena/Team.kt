package com.creeperface.nukkit.bedwars.api.arena

import cn.nukkit.inventory.Inventory
import com.creeperface.nukkit.bedwars.api.arena.configuration.IArenaConfiguration
import com.creeperface.nukkit.bedwars.api.shop.ShopMenuWindow

interface Team : IArenaConfiguration.ITeamConfiguration {

    val id: Int

    val arena: Arena

    val enderChest: Inventory

    val shop: ShopMenuWindow

    fun hasBed(): Boolean

    fun messagePlayers(message: String)

    fun getTeamPlayers(): Map<String, PlayerData>
}