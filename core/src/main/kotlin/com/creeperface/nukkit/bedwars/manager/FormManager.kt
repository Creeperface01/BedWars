package com.creeperface.nukkit.bedwars.manager

import cn.nukkit.Player
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.kformapi.KFormAPI
import com.creeperface.nukkit.kformapi.form.util.showForm

class FormManager(private val plugin: BedWars) {

    fun <T> createArenaSelection(p: Player, callback: (Arena) -> T) {
        val form = KFormAPI.simpleForm {
            title("Arena select")

            plugin.ins.values.forEach { arena ->
                button(arena.name) {
                    callback(arena)
                }
            }
        }

        p.showForm(form)
    }
}