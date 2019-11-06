package com.creeperface.nukkit.bedwars.api.arena

interface Arena {

    val players: Map<String, PlayerData>

    enum class ArenaState {
        LOBBY,
        GAME
    }
}