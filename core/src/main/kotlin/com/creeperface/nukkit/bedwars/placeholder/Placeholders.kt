package com.creeperface.nukkit.bedwars.placeholder

import cn.nukkit.utils.DyeColor
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.arena.Arena
import com.creeperface.nukkit.bedwars.api.placeholder.ArenaScope
import com.creeperface.nukkit.bedwars.api.placeholder.TeamScope
import com.creeperface.nukkit.bedwars.utils.rgb
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI

object Placeholders {

    private const val PREFIX = "bw_"

    fun init(plugin: BedWars) {
        plugin.server.pluginManager.getPlugin("PlaceholderAPI") ?: return

        val api = PlaceholderAPI.getInstance()

        //global placeholders
        api.build<Collection<String>>("${PREFIX}arenas") {
            loader {
                plugin.ins.values.map { it.name }
            }
        }

        //arena placeholders

        api.build<Collection<String>>("${PREFIX}arena_players") {
            scopedLoader(ArenaScope) {
                contextVal.players.values.map { it.player.name }
            }
        }

        api.build<Collection<String>>("${PREFIX}arena_spectators") {
            scopedLoader(ArenaScope) {
                contextVal.spectators.values.map { it.player.name }
            }
        }

        api.build<Arena.ArenaState>("${PREFIX}arena_state") {
            scopedLoader(ArenaScope) {
                contextVal.arenaState
            }
        }

        api.build<Boolean>("${PREFIX}arena_starting") {
            scopedLoader(ArenaScope) {
                contextVal.starting
            }
        }

        api.build<Boolean>("${PREFIX}arena_ending") {
            scopedLoader(ArenaScope) {
                contextVal.ending
            }
        }

        api.build<String>("${PREFIX}arena_map") {
            scopedLoader(ArenaScope) {
                contextVal.map
            }
        }

        //team placeholders
        api.build<DyeColor>("${PREFIX}team_color") {
            scopedLoader(TeamScope) {
                contextVal.color
            }
        }

        api.build<Int>("${PREFIX}team_color_rgb") {
            scopedLoader(TeamScope) {
                contextVal.color.rgb
            }
        }

        api.build<TextFormat>("${PREFIX}team_chat_color") {
            scopedLoader(TeamScope) {
                contextVal.chatColor
            }
        }

        api.build<String>("${PREFIX}team_name") {
            scopedLoader(TeamScope) {
                contextVal.name
            }
        }

        api.build<Boolean>("${PREFIX}team_bed") {
            scopedLoader(TeamScope) {
                contextVal.hasBed()
            }

            aliases("${PREFIX}team_has_bed")
        }

        api.build<Collection<String>>("${PREFIX}team_players") {
            scopedLoader(TeamScope) {
                contextVal.getTeamPlayers().values.map { it.player.name }
            }
        }
    }
}