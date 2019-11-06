package com.creeperface.nukkit.bedwars.arena.manager

import cn.nukkit.Player
import cn.nukkit.entity.projectile.EntityProjectile
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.player.PlayerDeathEvent
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.mysql.Stat
import com.creeperface.nukkit.bedwars.obj.Language

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
                    this.plugin.messageAllPlayers(Language.SHOT, pColor + p.name, dColor + killer.getName())
                    plugin.getPlayerData(killer)!!.add(Stat.KILLS)
                    return
                }
            } else if (lastDmg is EntityDamageByEntityEvent) {
                val killer = lastDmg.damager
                if (killer is Player) {
                    dColor = "" + this.plugin.getPlayerTeam(killer)!!.chatColor
                    this.plugin.messageAllPlayers(Language.CONTACT_PLAYER, pColor + p.name, dColor + killer.getName())
                    plugin.getPlayerData(killer)!!.add(Stat.KILLS)
                    return
                }
            }

            val killer: String?
            lateinit var killerName: String

            if (data.wasKilled()) {
                escape = true
                dColor = data.killerColor
                killer = data.killer

                killerName = dColor ?: "" + killer ?: ""

                val kData = plugin.playerData[killer!!.toLowerCase()]

                kData?.add(Stat.KILLS)
            }


            /*if($lastDmg instanceof EntityDamageByBlockEvent){
                if($escape === true){
                    $this->plugin->messageAllPlayers($pColor."{$p->getName()}".TextFormat::GRAY." walked into a cactus while trying to escape ".$this->plugi->getTeamColor($this->plugin->getPlayerTeam($killer)).$killer->getName());
                    $this->plugin->mysql->addKill($killer->getName());
                    return;
                }
                $this->plugin->messageAllPlayers($pColor."{$p->getName()}".TextFormat::GRAY." was pricked to death");
                return;
            }*/

            val playerName = pColor + p.name

            apply {
                when (lastDmg.cause) {
                    EntityDamageEvent.DamageCause.CONTACT -> {
                        if (escape) {
                            plugin.messageAllPlayers(Language.CACTUS_ESCAPE, playerName, killerName)
                            return
                        }
                        plugin.messageAllPlayers(Language.CACTUS, playerName)
                    }
                    EntityDamageEvent.DamageCause.SUFFOCATION -> plugin.messageAllPlayers(Language.SUFFOCATE, playerName)
                    EntityDamageEvent.DamageCause.FALL -> {
                        if (escape) {
                            plugin.messageAllPlayers(Language.FALL_ESCAPE, playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers(Language.FALL, playerName)
                    }
                    EntityDamageEvent.DamageCause.FIRE -> {
                        if (escape) {
                            plugin.messageAllPlayers(Language.FIRE_ESCAPE, playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers(Language.FIRE, playerName)
                    }
                    EntityDamageEvent.DamageCause.FIRE_TICK -> {
                        if (escape) {
                            plugin.messageAllPlayers(Language.FIRE_TICK_ESCAPE, playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers(Language.FIRE_TICK, playerName)
                    }
                    EntityDamageEvent.DamageCause.LAVA -> {
                        if (escape) {
                            plugin.messageAllPlayers(Language.LAVA_ESCAPE, playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers(Language.LAVA, playerName)
                    }
                    EntityDamageEvent.DamageCause.DROWNING -> {
                        if (escape) {
                            plugin.messageAllPlayers(Language.DROWNING_ESCAPE, playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers(Language.DROWNING, playerName)
                    }
                    EntityDamageEvent.DamageCause.BLOCK_EXPLOSION, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION -> plugin.messageAllPlayers(Language.EXPLOSION, playerName)
                    EntityDamageEvent.DamageCause.VOID -> {
                        if (escape) {
                            plugin.messageAllPlayers(Language.FALL_ESCAPE, playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers(Language.VOID, playerName)
                    }
                    else -> plugin.messageAllPlayers(Language.UNKNOWN, playerName)
                }
            }
        }
    }
}