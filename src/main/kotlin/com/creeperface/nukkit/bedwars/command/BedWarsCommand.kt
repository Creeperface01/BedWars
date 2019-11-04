package com.creeperface.nukkit.bedwars.command

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import com.creeperface.nukkit.bedwars.BedWars

class BedWarsCommand(plugin: BedWars) : BaseCommand("bedwars", plugin) {

    init {
        this.permission = "bedwars.command"
        this.usageMessage = "Use /bw help"

        this.commandParameters.clear()
        this.commandParameters["randomjoin"] = arrayOf(
                CommandParameter("action", arrayOf("randomjoin")),
                CommandParameter("player", CommandParamType.TARGET, true)
        )
    }

    override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false

        when (args[0]) {
            "randomjoin" -> {
                val p = when {
                    args.size == 2 -> plugin.server.getPlayerExact(args[1])
                    sender is Player -> sender
                    else -> return false
                }

                if (!p.hasPermission("bedwars.command.randomjoin")) {
                    return false
                }

                plugin.joinRandomArena(p)
            }
        }

        return true
    }
}