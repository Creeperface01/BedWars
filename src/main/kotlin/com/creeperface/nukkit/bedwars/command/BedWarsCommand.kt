package com.creeperface.nukkit.bedwars.command

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.api.event.ArenaStopEvent
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.listener.CommandEventListener.Action

class BedWarsCommand(plugin: BedWars) : BaseCommand("bedwars", plugin) {

    init {
        this.aliases = arrayOf("bw")
        this.permission = "bedwars.command"
        this.usageMessage = "Use /bw help"

        this.commandParameters.clear()
        this.commandParameters["quickjoin"] = arrayOf(
            CommandParameter.newEnum("action", arrayOf("quickjoin")),
            CommandParameter.newType("player", true, CommandParamType.TARGET)
        )
        this.commandParameters["sign"] = arrayOf(
            CommandParameter.newEnum("action", arrayOf("sign")),
        )
        this.commandParameters["stats"] = arrayOf(
            CommandParameter.newEnum("action", arrayOf("stats")),
            CommandParameter.newType("player", true, CommandParamType.TARGET)
        )
        this.commandParameters["help"] = arrayOf(
            CommandParameter.newEnum("action", arrayOf("help")),
        )
//        plugin.arenas.values.forEach { arena ->
//            this.commandParameters["teamsign" + arena.name] = arrayOf(
//                    CommandParameter("action", arrayOf("sign")),
//                    CommandParameter("arena", arena.name),
//                    CommandParameter("team", CommandParamType.INT, false)
//            )
//        }
    }

    override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(Lang.CMD_HELP.translatePrefix())
            return true
        }

        when (args[0]) {
            "quickjoin" -> {
                val p = when {
                    args.size >= 2 -> plugin.server.getPlayerExact(args[1])
                    sender is Player -> sender
                    else -> {
                        sender.sendMessage(Lang.COMMAND_IN_GAME.translate())
                        return true
                    }
                }

                if (p == null) {
                    sender.sendMessage(Lang.PLAYER_NOT_FOUND.translate(args[1]))
                    return true
                }

                if (!testPermission(p, "bedwars.command.quickjoin")) {
                    return true
                }

                plugin.joinRandomArena(p)
            }
            "stats" -> {
                val p = when {
                    args.size == 2 -> plugin.server.getPlayerExact(args[1])
                    sender is Player -> sender
                    else -> {
                        sender.sendMessage(Lang.COMMAND_IN_GAME.translate())
                        return true
                    }
                }

                if (p == null) {
                    sender.sendMessage(Lang.PLAYER_NOT_FOUND.translate(args[1]))
                    return true
                }

                if (p === sender && !testPermission(sender, "bedwars.command.stats")) {
                    return true
                }

                if (p !== sender && !testPermission(sender, "bedwars.command.stats.others")) {
                    return true
                }

                val data = plugin.players[p.id] ?: return true

                val stats = data.stats

                sender.sendMessage(
                    Lang.STATS.translate(
                        stats[Stat.KILLS].toString(),
                        stats[Stat.DEATHS].toString(),
                        stats[Stat.WINS].toString(),
                        stats[Stat.LOSSES].toString(),
                        stats[Stat.BEDS].toString()
                    )
                )
            }
            "help" -> {
                var msg = BedWars.chatPrefix + Lang.AVAILABLE_COMMANDS.translate() + ":\n"

                if (sender.hasPermission("bedwars.command.quickjoin")) {
                    msg += Lang.CMD_QUICKJOIN_HELP.translate() + "\n"
                }

                if (sender.hasPermission("bedwars.command.stats")) {
                    msg += Lang.CMD_STATS_HELP.translate() + "\n"
                }

                if (sender.hasPermission("bedwars.command.sign")) {
                    msg += Lang.CMD_SIGN_HELP.translate() + "\n"
                    msg += Lang.CMD_TEAMSIGN_HELP.translate() + "\n"
                }

                if (sender.hasPermission("bedwars.command.start")) {
                    msg += Lang.CMD_START_HELP.translate() + "\n"
                }

                if (sender.hasPermission("bedwars.command.stop")) {
                    msg += Lang.CMD_STOP_HELP.translate() + "\n"
                }

                if (sender.hasPermission("bedwars.command.vote")) {
                    msg += Lang.CMD_VOTE_HELP.translate() + "\n"
                }

                sender.sendMessage(msg)
            }
            else -> {
                if (sender !is Player) {
                    sender.sendMessage(Lang.COMMAND_IN_GAME.translatePrefix())
                    return false
                }

                when (args[0]) {
                    "sign" -> {
                        if (!testPermission(sender, "bedwars.command.sign")) {
                            return true
                        }

                        if (args.size != 1) {
                            sender.sendMessage(Lang.CMD_SIGN_HELP.translatePrefix())
                            return true
                        }

                        plugin.commandListener.actionPlayers[sender.uniqueId] = Action.SET_SIGN
                        sender.sendMessage(Lang.CMD_SIGN_ACTION.translatePrefix())
                    }
                    "teamsign" -> {
                        if (!testPermission(sender, "bedwars.command.sign")) {
                            return true
                        }

                        if (args.size != 3) {
                            sender.sendMessage(Lang.CMD_TEAMSIGN_HELP.translatePrefix())
                            return true
                        }

                        val arena = plugin.getArena(args[1])
                        if (arena == null) {
                            sender.sendMessage(Lang.ARENA_NOT_FOUND.translatePrefix(args[1]))
                            return true
                        }

                        val team = args[2].toIntOrNull()?.let {
                            arena.getTeam(it)
                        }
                        if (team == null) {
                            sender.sendMessage(Lang.ARENA_NOT_FOUND.translatePrefix(args[1]))
                            return true
                        }

                        plugin.commandListener.actionPlayers[sender.uniqueId] = Action.SET_TEAM_SIGN
                        sender.sendMessage(Lang.CMD_SIGN_ACTION.translatePrefix())
                    }
                    else -> {
                        val arena = plugin.getPlayerArena(sender)

                        if (arena == null) {
                            sender.sendMessage(Lang.COMMAND_IN_GAME.translatePrefix())
                            return true
                        }


                        when (args[0]) {
                            "start" -> {
                                if (!testPermission(sender, "bedwars.command.start")) {
                                    return true
                                }

                                arena.selectMap(true)
                            }
                            "stop" -> {
                                if (!testPermission(sender, "bedwars.command.stop")) {
                                    return true
                                }

                                arena.stopGame(ArenaStopEvent.Cause.COMMAND)
                            }
                            "vote" -> {
                                if (!testPermission(sender, "bedwars.command.vote")) {
                                    return true
                                }

                                if (args.size != 2) {
                                    sender.sendMessage(Lang.USE_PREFIX.translatePrefix() + " " + Lang.CMD_VOTE_HELP.translate())
                                    return true
                                }

                                arena.votingManager.onVote(sender, args[1].toLowerCase())
                                return true
                            }
                        }
                    }
                }
            }
        }

        return true
    }
}