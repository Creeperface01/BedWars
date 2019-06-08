package bedWars.arena

import bedWars.mySQL.Stat
import cn.nukkit.Player
import cn.nukkit.entity.projectile.EntityProjectile
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.player.PlayerDeathEvent

class DeathManager(var plugin: Arena) {

    fun onDeath(e: PlayerDeathEvent) {
        val p = e.entity
        val data = plugin.getPlayerData(p)

        val lastDmg = p.lastDamageCause
        val pColor = "" + data!!.team!!.color
        val dColor: String?
        var escape = false

        if (lastDmg != null) {
            if (lastDmg is EntityDamageByChildEntityEvent) {
                val arrow = lastDmg.child
                val killer = lastDmg.damager
                if (arrow is EntityProjectile && killer is Player) {
                    dColor = "" + this.plugin.getPlayerTeam(killer)!!.color
                    this.plugin.messageAllPlayers("shot", pColor + p.name, dColor + killer.getName())
                    plugin.getPlayerData(killer)!!.add(Stat.KILLS)
                    return
                }
            } else if (lastDmg is EntityDamageByEntityEvent) {
                val killer = lastDmg.damager
                if (killer is Player) {
                    dColor = "" + this.plugin.getPlayerTeam(killer)!!.color
                    this.plugin.messageAllPlayers("contact_player", pColor + p.name, dColor + killer.getName())
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
                            plugin.messageAllPlayers("cactus_escape", playerName, killerName)
                            return
                        }
                        plugin.messageAllPlayers("cactus", playerName)
                    }
                    EntityDamageEvent.DamageCause.SUFFOCATION -> plugin.messageAllPlayers("suffocate", playerName)
                    EntityDamageEvent.DamageCause.FALL -> {
                        if (escape) {
                            plugin.messageAllPlayers("fall_escape", playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers("fall", playerName)
                    }
                    EntityDamageEvent.DamageCause.FIRE -> {
                        if (escape) {
                            plugin.messageAllPlayers("fire_escape", playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers("fire", playerName)
                    }
                    EntityDamageEvent.DamageCause.FIRE_TICK -> {
                        if (escape) {
                            plugin.messageAllPlayers("fire_tick_escape", playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers("fire_tick", playerName)
                    }
                    EntityDamageEvent.DamageCause.LAVA -> {
                        if (escape) {
                            plugin.messageAllPlayers("lava_escape", playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers("lava", playerName)
                    }
                    EntityDamageEvent.DamageCause.DROWNING -> {
                        if (escape) {
                            plugin.messageAllPlayers("drowning_escape", playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers("drowning", playerName)
                    }
                    EntityDamageEvent.DamageCause.BLOCK_EXPLOSION, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION -> plugin.messageAllPlayers("explosion", playerName)
                    EntityDamageEvent.DamageCause.VOID -> {
                        if (escape) {
                            plugin.messageAllPlayers("fall_escape", playerName, killerName)
                            return@apply
                        }
                        plugin.messageAllPlayers("void", playerName)
                    }
                    else -> plugin.messageAllPlayers("unknown", playerName)
                }
            }
        }
    }
}