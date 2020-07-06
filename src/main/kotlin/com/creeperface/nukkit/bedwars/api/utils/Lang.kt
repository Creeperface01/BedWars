package com.creeperface.nukkit.bedwars.api.utils

import cn.nukkit.utils.Config
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.utils.logError
import com.creeperface.nukkit.bedwars.utils.logInfo

enum class Lang(
        private val prefix: String? = null,
        private val full: Boolean = false
) {
    STATS("general"),
    COMMAND_IN_GAME("general"),
    NOT_GAME_COMMAND("general"),
    AVAILABLE_COMMANDS("general"),
    PLAYER_NOT_FOUND("general"),
    USE_PREFIX("general"),
    ARENA_NOT_FOUND("general"),

    USE_VOTE("arena"),
    PLAYER_LEAVE("arena"),
    BED_BREAK("arena"),
    TEAM_JOIN("arena"),
    END_GAME("arena"),
    VOTE("arena"),
    PE_ONLY("arena"),
    FULL_TEAM("arena"),
    ALREADY_IN_TEAM("arena"),
    CAN_NOT_VOTE("arena"),
    GAME_FULL("arena"),
    BREAK_OWN_BED("arena"),
    JOIN_SPECTATOR("arena"),
    LEGEND_FOUND("arena"),
    MIN_PLAYERS("arena"),
    SELECT_MAP("arena"),
    NO_ARENA_FOUND("arena"),
    PLAYER_COUNT("arena"),
    JOIN("arena"),
    START_GAME("arena"),
    LEAVE("arena"),
    GAME_IN_PROGRESS("arena"),

    CMD_HELP("command.bedwars.help", true),
    CMD_QUICKJOIN_HELP("command.quickjoin.help", true),
    CMD_STATS_HELP("command.stats.help", true),
    CMD_SIGN_HELP("command.sign.help", true),
    CMD_SIGN_ACTION("command.sign.action", true),
    CMD_TEAMSIGN_HELP("command.teamsign.help", true),
    CMD_START_HELP("command.start.help", true),
    CMD_STOP_HELP("command.stop.help", true),
    CMD_VOTE_HELP("command.vote.help", true),
    CMD_TEAM_HELP("command.team.help", true),

    BUY("shop"),
    SHOP_ITEM("shop"),
    SHOP_COST("shop"),
    HIGH_COST("shop"),
    FULL_INVENTORY("shop"),

    FALL("death"),
    FIRE_TICK("death"),
    SHOT("death"),
    SUFFOCATE("death"),
    CACTUS("death"),
    FIRE_ESCAPE("death"),
    UNKNOWN("death"),
    LAVA("death"),
    FIRE_TICK_ESCAPE("death"),
    FIRE("death"),
    VOID("death"),
    LAVA_ESCAPE("death"),
    DROWNING("death"),
    FALL_ESCAPE("death"),
    CONTACT_PLAYER("death"),
    CONTACT("death"),
    CACTUS_ESCAPE("death"),
    DROWNING_ESCAPE("death"),
    EXPLOSION("death"),
    ;

    private lateinit var translation: String

    fun translate(vararg args: Any) = translate0(false, *args)

    fun translatePrefix(vararg args: Any) = translate0(true, *args)

    private fun translate0(prefix: Boolean, vararg args: Any): String {
        if (args.isEmpty()) return (if(prefix) BedWars.chatPrefix else "") + this.translation

        val base = StringBuilder(this.translation)

        for (i in args.indices) {
            base.replaceAll("%$i", args[i].toString())
        }

        if (prefix) {
            base.insert(0, BedWars.chatPrefix)
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
            for (value in values()) {
                var key = (value.prefix?.let { "$it." } ?: "") + if (value.full) "" else value.name.toLowerCase()

                if (key.endsWith(".")) {
                    key = key.substring(0, key.length - 1)
                }

                val cfgValue = data.get(key)

                if (cfgValue == null) {
                    logError("Unknown language translation $key")
                    continue
                }

                value.translation = cfgValue.toString().replace('&', TextFormat.ESCAPE)
            }
        }
    }
}
