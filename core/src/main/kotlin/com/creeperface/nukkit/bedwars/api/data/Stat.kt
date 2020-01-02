package com.creeperface.nukkit.bedwars.api.data

enum class Stat {
    KILLS,
    DEATHS,
    WINS,
    LOSSES,
    BEDS,
    PLACE,
    BREAK,
    GAMES;

    private val statName: String = name.toLowerCase()

    fun getName() = statName
}
