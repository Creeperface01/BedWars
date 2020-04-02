package com.creeperface.nukkit.bedwars.command

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.listener.CommandEventListener

class BedWarsCommand(plugin: BedWars) : BaseCommand("bedwars", plugin) {

    init {
        this.aliases = arrayOf("bw")
        this.permission = "bedwars.command"
        this.usageMessage = "Use /bw help"

        this.commandParameters.clear()
        this.commandParameters["randomjoin"] = arrayOf(
                CommandParameter("action", arrayOf("randomjoin")),
                CommandParameter("player", CommandParamType.TARGET, true)
        )
        this.commandParameters["sign"] = arrayOf(
                CommandParameter("action", arrayOf("sign")),
                CommandParameter("arena", plugin.arenas.keys.toTypedArray())
        )
        plugin.arenas.values.forEach { arena ->
            this.commandParameters["teamsign" + arena.name] = arrayOf(
                    CommandParameter("action", arrayOf("sign")),
                    CommandParameter("arena", arena.name),
                    CommandParameter("team", CommandParamType.INT, false)
            )
        }
    }

    override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false

        when (args[0]) {
            "quickjoin" -> {
                val p = when {
                    args.size == 2 -> plugin.server.getPlayerExact(args[1])
                    sender is Player -> sender
                    else -> return false
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
                    else -> return false
                }

                if (p === sender && !testPermission(sender, "bedwars.command.stats")) {
                    return true
                }

                if (p !== sender && !testPermission(sender, "bedwars.command.stats.others")) {
                    return true
                }

                val data = plugin.players[p.id] ?: return true

                val stats = data.stats

                sender.sendMessage(Lang.STATS.translate(stats[Stat.KILLS].toString(), stats[Stat.DEATHS].toString(), stats[Stat.WINS].toString(), stats[Stat.LOSSES].toString(), stats[Stat.BEDS].toString()))
            }
            "help" -> {

            }
            else -> {
                if (sender !is Player) {
                    sender.sendMessage(BedWars.prefix + TextFormat.RED + "You can run this command only in-game")
                    return false
                }

                when (args[0]) {
                    "sign" -> {
                        if (!testPermission(sender, "bedwars.command.sign")) {
                            return true
                        }

                        plugin.commandListener.actionPlayers[sender.uniqueId] = CommandEventListener.Action.SET_SIGN
                        sender.sendMessage(BedWars.prefix + TextFormat.YELLOW + "Click on the sign to set")
                    }
                    "teamsign" -> {
                        if (!testPermission(sender, "bedwars.command.sign")) {
                            return true
                        }

                        plugin.commandListener.actionPlayers[sender.uniqueId] = CommandEventListener.Action.SET_TEAM_SIGN
                        sender.sendMessage(BedWars.prefix + TextFormat.YELLOW + "Click on the sign to set")
                    }
                    else -> {
                        val arena = plugin.getPlayerArena(sender)

                        if (arena == null) {
                            sender.sendMessage(Lang.COMMAND_IN_GAME.translate())
                            return true
                        }


                        when (args[0]) {
                            "start" -> {
                                if (!testPermission(sender, "bedwars.command.start")) {
                                    return true
                                }
                            }
                            "stop" -> {
                                if (!testPermission(sender, "bedwars.command.stop")) {
                                    return true
                                }
                            }
                            "vote" -> {
                                if (!testPermission(sender, "bedwars.command.vote")) {
                                    return true
                                }

                                if (args.size != 1) {
                                    return false
                                }

                                arena.votingManager.onVote(sender, args[0].toLowerCase())
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