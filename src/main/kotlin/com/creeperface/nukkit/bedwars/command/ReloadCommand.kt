package com.creeperface.nukkit.bedwars.command

import cn.nukkit.command.CommandSender
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.utils.plus

class ReloadCommand(plugin: BedWars) : BaseCommand("reload", plugin) {

    init {
        this.description = "%nukkit.command.reload.description"
        this.usageMessage = "%commands.reload.usage"
        this.permission = "nukkit.command.reload"
        this.commandParameters.clear()
    }

    override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (!this.testPermission(sender)) {
            return true
        }

        sender.sendMessage(TextFormat.RED + "You can't use this command while BedWars is enabled")
        return true
    }
}