package com.creeperface.nukkit.bedwars.arena.manager

import cn.nukkit.Player
import cn.nukkit.entity.projectile.EntityProjectile
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.player.PlayerDeathEvent
import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.arena.handler.ArenaGame


fun ArenaGame.onDeath(e: PlayerDeathEvent) {
    val p = e.entity
    val data = getPlayerData(p) ?: return

    val lastDmg = p.lastDamageCause
    val pColor = data.team.chatColor.toString()
    val dColor: String?
    var escape = false

    if (lastDmg != null) {
        if (lastDmg is EntityDamageByChildEntityEvent) {
            val arrow = lastDmg.child
            val killer = lastDmg.damager
            if (arrow is EntityProjectile && killer is Player) {
                dColor = "" + getPlayerTeam(killer)!!.chatColor
                messageAllPlayers(Lang.SHOT, pColor + p.name, dColor + killer.getName())
                getPlayerData(killer)!!.addStat(Stat.KILLS)
                return
            }
        } else if (lastDmg is EntityDamageByEntityEvent) {
            val killer = lastDmg.damager
            if (killer is Player) {
                dColor = "" + getPlayerTeam(killer)!!.chatColor
                messageAllPlayers(Lang.CONTACT_PLAYER, pColor + p.name, dColor + killer.getName())
                getPlayerData(killer)!!.addStat(Stat.KILLS)
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

            val kData = playerData[killer!!.toLowerCase()]

            kData?.addStat(Stat.KILLS)
        }

        val playerName = pColor + p.name

        apply {
            when (lastDmg.cause) {
                EntityDamageEvent.DamageCause.CONTACT -> {
                    if (escape) {
                        messageAllPlayers(Lang.CACTUS_ESCAPE, playerName, killerName)
                        return
                    }
                    messageAllPlayers(Lang.CACTUS, playerName)
                }
                EntityDamageEvent.DamageCause.SUFFOCATION -> messageAllPlayers(Lang.SUFFOCATE, playerName)
                EntityDamageEvent.DamageCause.FALL -> {
                    if (escape) {
                        messageAllPlayers(Lang.FALL_ESCAPE, playerName, killerName)
                        return@apply
                    }
                    messageAllPlayers(Lang.FALL, playerName)
                }
                EntityDamageEvent.DamageCause.FIRE -> {
                    if (escape) {
                        messageAllPlayers(Lang.FIRE_ESCAPE, playerName, killerName)
                        return@apply
                    }
                    messageAllPlayers(Lang.FIRE, playerName)
                }
                EntityDamageEvent.DamageCause.FIRE_TICK -> {
                    if (escape) {
                        messageAllPlayers(Lang.FIRE_TICK_ESCAPE, playerName, killerName)
                        return@apply
                    }
                    messageAllPlayers(Lang.FIRE_TICK, playerName)
                }
                EntityDamageEvent.DamageCause.LAVA -> {
                    if (escape) {
                        messageAllPlayers(Lang.LAVA_ESCAPE, playerName, killerName)
                        return@apply
                    }
                    messageAllPlayers(Lang.LAVA, playerName)
                }
                EntityDamageEvent.DamageCause.DROWNING -> {
                    if (escape) {
                        messageAllPlayers(Lang.DROWNING_ESCAPE, playerName, killerName)
                        return@apply
                    }
                    messageAllPlayers(Lang.DROWNING, playerName)
                }
                EntityDamageEvent.DamageCause.BLOCK_EXPLOSION, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION -> messageAllPlayers(
                    Lang.EXPLOSION,
                    playerName
                )
                EntityDamageEvent.DamageCause.VOID -> {
                    if (escape) {
                        messageAllPlayers(Lang.FALL_ESCAPE, playerName, killerName)
                        return@apply
                    }
                    messageAllPlayers(Lang.VOID, playerName)
                }
                else -> messageAllPlayers(Lang.UNKNOWN, playerName)
            }
        }
    }
}