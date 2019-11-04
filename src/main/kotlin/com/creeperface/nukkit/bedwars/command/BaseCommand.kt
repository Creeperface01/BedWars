package com.creeperface.nukkit.bedwars.command

import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import com.creeperface.nukkit.bedwars.BedWars

abstract class BaseCommand(name: String, protected val plugin: BedWars) : Command(name) {

    init {
        description = "BedWars command"
        usageMessage = ""
    }

    abstract override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean
}