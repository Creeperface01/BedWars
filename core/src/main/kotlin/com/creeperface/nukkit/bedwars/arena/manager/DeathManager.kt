package com.creeperface.nukkit.bedwars.arena.manager

import cn.nukkit.Player
import cn.nukkit.entity.projectile.EntityProjectile
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.player.PlayerDeathEvent
import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.arena.Arena

class DeathManager(var plugin: Arena) {

    fun onDeath(e: PlayerDeathEvent) {
        val p = e.entity
        val data = plugin.getPlayerData(p) ?: return

        val lastDmg = p.lastDamageCause
        val pColor = data.team.chatColor.toString()
        val dColor: String?
        var escape = false

        if (lastDmg != null) {
            if (lastDmg is EntityDamageByChildEntityEvent) {
                val arrow = lastDmg.child
                val killer = lastDmg.damager
                if (arrow is EntityProjectile && killer is Player) {
                    dColor = "" + this.plugin.getPlayerTeam(killer)!!.chatColor
                    this.plugin.messageAllPlayers(Lang.SHOT, pColor + p.name, dColor + killer.getName())
                    plugin.getPlayerData(killer)!!.addStat(Stat.KILLS)
                    return
                }
            } else if (lastDmg is EntityDamageByEntityEvent) {
                val killer = lastDmg.damager
                if (killer is Player) {
                    dColor = "" + this.plugin.getPlayerTeam(killer)!!.chatColor
                    this.plugin.messageAllPlayers(Lang.CONTACT_PLAYER, pColor + p.name, dColor + killer.getName())
                    plugin.getPlayerData(killer)!!.addStat(Stat.KILLS)
                    return
                }
            }

            val killer: String?
            lateinit var killerName: String

            if (data.wasKilled()) {
                escape = true
                dColor = data.killerColor
                killer = data.killer

                killerName = (dColor ?: "") + (killer ?: "")

                val kData = plugin.playerData[killer!!.toLowerCase()]

                kData?.addStat(Stat.KILLS)
            }

            val playerName = pColor + p.name

            apply {
                when (lastDmg.cause) {
                    EntityDamageEvent.DamageCause.CONTACT -> {
                        if (escape) {
                            plugin.messageAllPlayers(Lang.CACTUS_ESCAPE, playerName, killerName)
                            return
                        }
                        plugin.messageAllPlayers(Lang.CACTUS, playerName)
                    }
                    EntityDamageEvent.DamageCause.SUFFOCATION -> plugin.messageAllPlayers(Lang.SUFFOCATE, playerName)
                    EntityDamageEvent.DamageCause.FALL -> {
                        if (escape) {
                            plugin.messageAllPlayers(Lang.FALL_ESCAPE, playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers(Lang.FALL, playerName)
                    }
                    EntityDamageEvent.DamageCause.FIRE -> {
                        if (escape) {
                            plugin.messageAllPlayers(Lang.FIRE_ESCAPE, playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers(Lang.FIRE, playerName)
                    }
                    EntityDamageEvent.DamageCause.FIRE_TICK -> {
                        if (escape) {
                            plugin.messageAllPlayers(Lang.FIRE_TICK_ESCAPE, playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers(Lang.FIRE_TICK, playerName)
                    }
                    EntityDamageEvent.DamageCause.LAVA -> {
                        if (escape) {
                            plugin.messageAllPlayers(Lang.LAVA_ESCAPE, playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers(Lang.LAVA, playerName)
                    }
                    EntityDamageEvent.DamageCause.DROWNING -> {
                        if (escape) {
                            plugin.messageAllPlayers(Lang.DROWNING_ESCAPE, playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers(Lang.DROWNING, playerName)
                    }
                    EntityDamageEvent.DamageCause.BLOCK_EXPLOSION, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION -> plugin.messageAllPlayers(
                        Lang.EXPLOSION,
                        playerName
                    )
                    EntityDamageEvent.DamageCause.VOID -> {
                        if (escape) {
                            plugin.messageAllPlayers(Lang.FALL_ESCAPE, playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers(Lang.VOID, playerName)
                    }
                    else -> plugin.messageAllPlayers(Lang.UNKNOWN, playerName)
                }
            }
        }
    }
}