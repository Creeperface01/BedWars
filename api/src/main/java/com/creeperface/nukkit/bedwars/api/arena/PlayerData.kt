package com.creeperface.nukkit.bedwars.api.arena

import cn.nukkit.Player

interface PlayerData {

    val arena: Arena
    val player: Player
    val team: Team

    fun hasTeam(): Boolean
}