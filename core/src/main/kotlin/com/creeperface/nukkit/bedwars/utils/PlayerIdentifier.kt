package com.creeperface.nukkit.bedwars.utils

import cn.nukkit.Player

enum class PlayerIdentifier {
    NAME {
        override fun get(player: Player): String = player.name
    },
    UUID {
        override fun get(player: Player): java.util.UUID = player.uniqueId
    };

    abstract fun get(player: Player): Any
}