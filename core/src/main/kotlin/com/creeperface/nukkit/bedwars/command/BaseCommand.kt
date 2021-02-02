package com.creeperface.nukkit.bedwars.command

import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.lang.TranslationContainer
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.utils.TF

abstract class BaseCommand(name: String, protected val plugin: BedWars) : Command(name) {

    init {
        description = "BedWars command"
        usageMessage = ""
    }

    abstract override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean

    protected fun testPermission(target: CommandSender, permission: String): Boolean {
        return if (target.hasPermission(permission)) {
            true
        } else {
            if (this.permissionMessage == null) {
                target.sendMessage(
                    TranslationContainer(
                        TF.RED.toString() + "%commands.generic.unknown",
                        this.name
                    )
                )
            } else if (this.permissionMessage.isNotEmpty()) {
                target.sendMessage(this.permissionMessage.replace("<permission>", this.permission))
            }

            false
        }
    }
}