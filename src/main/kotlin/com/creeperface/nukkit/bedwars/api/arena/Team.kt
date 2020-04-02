package com.creeperface.nukkit.bedwars.api.arena

import cn.nukkit.inventory.Inventory
import com.creeperface.nukkit.bedwars.api.arena.configuration.IArenaConfiguration
import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.shop.ShopMenuWindow
import com.creeperface.nukkit.bedwars.api.utils.TeamContext

interface Team : MapConfiguration.ITeamData {

    val id: Int

    val arena: Arena

    val enderChest: Inventory

    val shop: ShopMenuWindow

    val context: TeamContext

    fun hasBed(): Boolean

    fun messagePlayers(message: String)

    fun getTeamPlayers(): Map<String, PlayerData>
}