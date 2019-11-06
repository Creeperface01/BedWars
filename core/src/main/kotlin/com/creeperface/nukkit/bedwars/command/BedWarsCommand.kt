package com.creeperface.nukkit.bedwars.command

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.listener.CommandEventListener

class BedWarsCommand(plugin: BedWars) : BaseCommand("bedwars", plugin) {

    init {
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
                    CommandParameter("team", arena.teamData.map { it.name }.toTypedArray())
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
                    return false
                }

                plugin.joinRandomArena(p)
            }
            "stats" -> {
                if (!testPermission(sender, "bedwars.command.stats")) {
                    return false
                }
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
                            return false
                        }

                        plugin.commandListener.actionPlayers[sender.uniqueId] = CommandEventListener.Action.SET_SIGN
                        sender.sendMessage(BedWars.prefix + TextFormat.YELLOW + "Click on the sign to set")
                    }
                    "teamsign" -> {
                        if (!testPermission(sender, "bedwars.command.sign")) {
                            return false
                        }

                        plugin.commandListener.actionPlayers[sender.uniqueId] = CommandEventListener.Action.SET_TEAM_SIGN
                        sender.sendMessage(BedWars.prefix + TextFormat.YELLOW + "Click on the sign to set")
                    }
                    "start" -> {
                        if (!testPermission(sender, "bedwars.command.start")) {
                            return false
                        }
                    }
                    "stop" -> {
                        if (!testPermission(sender, "bedwars.command.stop")) {
                            return false
                        }
                    }
                    "vote" -> {
                        if (!testPermission(sender, "bedwars.command.vote")) {
                            return false
                        }
                    }
                }
            }
        }

        return true
    }
}