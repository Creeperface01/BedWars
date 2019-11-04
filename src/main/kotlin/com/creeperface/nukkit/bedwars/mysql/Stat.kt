package com.creeperface.nukkit.bedwars.mysql

enum class Stat private constructor(val xp: Int, val tokens: Int) {
    KILLS(50, 1),
    DEATHS(0, 0),
    WINS(500, 10),
    LOSSES(0, 0),
    BEDS(200, 5),
    PLACE(1, 0),
    BREAK(1, 0),
    PLAYED(200, 0);
    //RESOURCES(1, 0);

    private val statName: String = name.toLowerCase().trim { it <= ' ' }

    fun getName() = statName
}
