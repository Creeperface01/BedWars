package com.creeperface.nukkit.bedwars.api.utils

import cn.nukkit.utils.Config

enum class Lang {
    USE_VOTE,
    PLAYER_LEAVE,
    BED_BREAK,
    PERMS,
    FALL,
    UPDATE_MSG,
    FIRE_TICK,
    TEAM_JOIN,
    END_GAME,
    VOTE,
    SHOT,
    CACTUS,
    FIRE_ESCAPE,
    LEGEND_FOUND,
    JOIN_SPECTATOR,
    STATS,
    PE_ONLY,
    UNKNOWN,
    LAVA,
    FULL_INVENTORY,
    SUFFOCATE,
    FULL_TEAM,
    ALREADY_IN_TEAM,
    CAN_NOT_VOTE,
    GAME_FULL,
    BREAK_OWN_BED,
    FIRE_TICK_ESCAPE,
    BUY,
    SHOP_ITEM,
    SHOP_COST,
    FIRE,
    VOID,
    MIN_PLAYERS,
    SELECT_MAP,
    TOKENS,
    HIGH_COST,
    NO_ARENA_FOUND,
    LAVA_ESCAPE,
    DROWNING,
    PLAYER_COUNT,
    JOIN,
    START_GAME,
    FALL_ESCAPE,
    CONTACT_PLAYER,
    LEAVE,
    CONTACT,
    CACTUS_ESCAPE,
    DROWNING_ESCAPE,
    EXPLOSION;

    private lateinit var translation: String

    fun translate(vararg args: Any): String {
        if (args.isEmpty()) return this.translation

        val base = StringBuilder(this.translation)

        for (i in args.indices) {
            base.replaceAll("%$i", args[i].toString())
        }

        return base.toString()
    }

    private fun StringBuilder.replaceAll(from: String, to: String) {
        var index = this.indexOf(from)

        while (index != -1) {
            this.replace(index, index + from.length, to)
            index += to.length
            index = this.indexOf(from, index)
        }
    }

    companion object {

        fun init(data: Config) {
            for ((key, value) in data.all) {
                valueOf(key.toUpperCase()).translation = value.toString()
            }
        }
    }
}
