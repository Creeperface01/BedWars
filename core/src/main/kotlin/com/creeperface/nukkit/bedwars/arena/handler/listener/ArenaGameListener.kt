package com.creeperface.nukkit.bedwars.arena.handler.listener

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.entity.projectile.EntityArrow
import cn.nukkit.event.EventHandler
import cn.nukkit.event.EventPriority
import cn.nukkit.event.Listener
import cn.nukkit.event.block.BlockBreakEvent
import cn.nukkit.event.block.BlockBurnEvent
import cn.nukkit.event.block.BlockPlaceEvent
import cn.nukkit.event.entity.*
import cn.nukkit.event.player.*
import cn.nukkit.form.response.FormResponseSimple
import cn.nukkit.item.Item
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.network.protocol.EntityEventPacket
import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.api.utils.BedWarsExplosion
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.arena.IArena
import com.creeperface.nukkit.bedwars.arena.handler.ArenaGame
import com.creeperface.nukkit.bedwars.arena.manager.onDeath
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityMine
import com.creeperface.nukkit.bedwars.entity.BWVillager
import com.creeperface.nukkit.bedwars.entity.TNTShip
import com.creeperface.nukkit.bedwars.obj.BedWarsData
import com.creeperface.nukkit.bedwars.utils.TF
import com.creeperface.nukkit.bedwars.utils.configuration
import com.creeperface.nukkit.bedwars.utils.openShopInventory
import com.creeperface.nukkit.placeholderapi.api.scope.context
import com.creeperface.nukkit.placeholderapi.api.util.translatePlaceholders
import java.util.*

class ArenaGameListener(private val arena: ArenaGame) : Listener {

//    fun register() {
//        val plugin = arena.plugin
//
//        register(plugin, this::onBlockTouch2)
//        register(plugin, this::onBlockTouch, EventPriority.HIGHEST, true)
//        register(plugin, this::onQuit)
//        register(plugin, this::onHit, ignoreCancelled = true)
//        register(plugin, this::onEntityInteract)
//        register(plugin, this::onBlockBreak, ignoreCancelled = true)
//        register(plugin, this::onBlockPlace)
//        register(plugin, this::onProjectileHit, EventPriority.LOWEST, true)
//        register(plugin, this::onChat, ignoreCancelled = true)
//        register(plugin, this::onFireSpread, ignoreCancelled = true)
//        register(plugin, this::onBowShot, ignoreCancelled = true)
//        register(plugin, this::onFormResponse)
//    }

    @EventHandler
    fun onBlockTouch2(e: PlayerInteractEvent) {
        val p = e.player

        if (arena.isSpectator(p)) {
            if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && e.item.id == Item.CLOCK) {
                arena.leaveArena(p)
                return
            }

            return
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onBlockTouch(e: PlayerInteractEvent) {
        val b = e.block
        val p = e.player
        val item = e.item

        if (e.action == PlayerInteractEvent.Action.PHYSICAL) {
            arena.onEntityInteract(e)
            return
        }

        val data = arena.getPlayerData(p) ?: return

//        if (this.arena.arenaState == ArenaState.LOBBY && e.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
//            val slot = p.inventory.heldItemIndex
//            if (slot == arena.lobbyItem?.slot) {
//                this.arena.leaveArena(p)
//                p.teleport(arena.plugin.server.defaultLevel.spawnLocation)
//                return
//            }
//
//            if (arena.teamSelect && slot == arena.teamSelectItem?.slot) {
//                arena.showTeamSelection(p)
//                return
//            }
//
//            if (arena.voting && slot == arena.voteConfig.item?.slot) {
//                arena.votingManager.showVotingSelection(p)
//                return
//            }
//
//            return
//        }

        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && b.id == Block.ENDER_CHEST) {
            e.setCancelled()
            p.addWindow(data.team.enderChest)
        }

        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && item.id == Item.SPAWN_EGG && item.damage == TNTShip.NETWORK_ID) {

            val nbt = CompoundTag()
                .putList(
                    ListTag<DoubleTag>("Pos")
                        .add(DoubleTag("", b.getX() + 0.5))
                        .add(DoubleTag("", b.getY() + 1))
                        .add(DoubleTag("", b.getZ() + 0.5))
                )
                .putList(
                    ListTag<DoubleTag>("Motion")
                        .add(DoubleTag("", 0.toDouble()))
                        .add(DoubleTag("", 0.toDouble()))
                        .add(DoubleTag("", 0.toDouble()))
                )
                .putList(
                    ListTag<FloatTag>("Rotation")
                        .add(FloatTag("", Random().nextFloat() * 360))
                        .add(FloatTag("", 0.toFloat()))
                )

            val ship = TNTShip(b.getLevel().getChunk(b.floorX shr 4, b.floorZ shr 4), nbt, this.arena, data.team)
            ship.spawnToAll()

            item.count--
            p.inventory.itemInHand = item
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        if (arena.inArena(e.player) || this.arena.isSpectator(e.player)) {
            this.arena.leaveArena(e.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onHit(e: EntityDamageEvent) {
        val victim = e.entity

//        if (arena.arenaState == ArenaState.LOBBY) {
//            if (victim is Player) {
//                if (this.arena.arenaState == ArenaState.LOBBY && e.cause == EntityDamageEvent.DamageCause.VOID && arena.inArena(victim)) {
//                    victim.teleport(this.arena.arenaLobby)
//                    e.setCancelled()
//                }
//            }
//
//            return
//        }

        var kill = false

        var data: BedWarsData? = null
        val kData: BedWarsData

        if (victim is Player) {
            if (arena.isSpectator(victim)) {
                e.setCancelled()
                return
            }

            if (!this.arena.inArena(victim)) {
                return
            }

//            if (this.arena.arenaState == ArenaState.LOBBY) {
//                e.setCancelled()
//                return
//            }

            if (victim.getGamemode() == 3) {
                e.setCancelled()

                if (e.cause == EntityDamageEvent.DamageCause.VOID) {
                    victim.setMotion(Vector3(0.0, 10.0, 0.0))
                }
            }

            if (victim.getHealth() - e.finalDamage < 1) {
                kill = true
                data = arena.getPlayerData(victim)
            }
        } else if (victim is BWVillager) {
            e.setCancelled()
        }

        if (e is EntityDamageByEntityEvent) {
            if (e.damager is Player) {
                val killer = e.damager as Player
                kData = arena.getPlayerData(killer) ?: return

                if (victim is Player) {
                    if (data == null) {
                        data = arena.getPlayerData(victim) ?: return
                    }

                    if (data.team === kData.team) {
                        e.setCancelled()
                        return
                    }

                    if (!kill) {
                        data.lastHit = System.currentTimeMillis()
                        data.killer = killer.name
                        data.killerColor = "" + kData.team.chatColor
                    }
                }

                if (e !is EntityDamageByChildEntityEvent && victim is BWVillager && killer.getGamemode() == 0) {
                    val inv = kData.team.shop

                    killer.openShopInventory(inv)

                    e.setCancelled()
                }
            }
        }

        if (kill) {
            e.setCancelled()

            val p = victim as Player

            val pk = EntityEventPacket()
            pk.eid = p.id
            pk.event = EntityEventPacket.HURT_ANIMATION
            p.dataPacket(pk)

            p.health = 20f
            p.foodData.foodSaturationLevel = 20f
            p.foodData.level = 20
            p.removeAllEffects()
            p.lastDamageCause = e

            val event = PlayerDeathEvent(p, arrayOfNulls(0), "", 0)

            this.arena.onDeath(event)

            data!!.addStat(Stat.DEATHS)

            p.inventory.clearAll()

            if (!data.canRespawn()) {
                this.arena.unsetPlayer(p)
                p.sendMessage(Lang.JOIN_SPECTATOR.translatePrefix())
                this.arena.setSpectator(p, true)

//                data.baseData.addExp(200) //played

                arena.checkAlive()
            } else {
                p.teleport(data.team.spawn)
            }
        }
    }

    @EventHandler
    fun onEntityInteract(e: PlayerInteractEntityEvent) {
        val p = e.player

        val data = arena.getPlayerData(p) ?: return

        p.openShopInventory(data.team.shop)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(e: BlockBreakEvent) {
        val p = e.player
        val b = e.block

        val data = arena.getPlayerData(p) ?: return

        if (e.isFastBreak || b.id != Block.BED_BLOCK && !IArena.allowedBlocks.contains(b.id)) {
            e.setCancelled()
            return
        }

        if (b.id == Block.STONE_PRESSURE_PLATE) {
            e.drops = arrayOfNulls(0)
        }

        if (this.arena.isSpectator(p)) {
            e.setCancelled()
            return
        }

//        if (this.arena.arenaState == ArenaState.LOBBY) {
//            e.setCancelled()
//            return
//        }

//        data.baseData.addExp(1)

        val isBed = arena.isBed(b)

        if (isBed != null/* && NukerCheck.run(p, b)*/) {
            e.isCancelled = !this.arena.onBedBreak(p, isBed, b)
            e.drops = arrayOfNulls(0)
            return
        }

        if (b.id == Item.SPONGE) {
            val randomItem = this.arena.luckyBlockItems.random()

            if (TF.clean(randomItem.customName).startsWith("Legendary")) { //TODO: do not hardcode
                arena.messageAllPlayers(
                    Lang.LEGEND_FOUND,
                    data.team.chatColor.toString() + p.name,
                    randomItem.customName
                )
            }

            e.drops = arrayOf(randomItem)
            return
        }

        if (b.id == Item.ENDER_CHEST) {
            e.drops = arrayOf(Item.get(Item.ENDER_CHEST))
        }
    }

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        val b = e.block
        val p = e.player

        val data = arena.getPlayerData(p) ?: return

        if (e.isCancelled || !IArena.allowedBlocks.contains(b.id)) {
            e.setCancelled()
            return
        }

        if (this.arena.isSpectator(p)) {
            e.setCancelled()
            return
        }

//        if (this.arena.arenaState == ArenaState.LOBBY) {
//            e.setCancelled()
//            return
//        }

        if (b.id == Block.STONE_PRESSURE_PLATE) {
            val nbt = CompoundTag()
                .putList(ListTag("Items"))
                .putString("id", BlockEntity.FURNACE)
                .putInt("x", b.floorX)
                .putInt("y", b.floorY)
                .putInt("z", b.floorZ)
                .putInt("team", data.team.id)

            BlockEntityMine(b.level.getChunk(b.floorX shr 4, b.floorZ shr 4), nbt)
        }

//        data.baseData.addExp(1)
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onProjectileHit(e: ProjectileHitEvent) {
        val ent = e.entity

        if (ent.getLevel().id != arena.level.id) {
            return
        }

        if (ent is EntityArrow && ent.namedTag.contains("explode")) {
            val explosion = BedWarsExplosion(ent, 0.8, ent)

            explosion.explode(this.arena, ent.namedTag.getInt("team"))
            ent.close()
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onChat(e: PlayerChatEvent) {
        val p = e.player

        if (arena.gameSpectators.containsKey(p.name.toLowerCase())) {
            val msg = configuration.spectatorFormat.translatePlaceholders(p, arena.context, e.context)

            for (s in arena.gameSpectators.values) {
                s.sendMessage(msg)
            }

            e.setCancelled()
            return
        }

        val data = arena.getPlayerData(p) ?: return
        e.setCancelled()

//        if (!data.hasTeam()) {
//            arena.messageAllPlayers(e.message, p, data)
//            return
//        }

        if (e.message.startsWith(configuration.allPrefix) && e.message.length > 1) {
            this.arena.messageAllPlayers(e.message.substring(configuration.allPrefix.length), p, data)
        } else {
            data.team.messagePlayers(e.message, data)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onFireSpread(e: BlockBurnEvent) {
        if (e.block.level === arena.level) {
            e.setCancelled()
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBowShot(e: EntityShootBowEvent) {
        val entity = e.entity
        val bow = e.bow

        if (entity is Player) {

            if (entity.gamemode > 1) {
                e.setCancelled()
            }

            val data = arena.getPlayerData(entity) ?: return

            if (bow.customName == "Explosive Bow") {
                val entityBow = e.projectile

                entityBow.namedTag.putBoolean("explode", true)
                entityBow.namedTag.putInt("team", data.team.id)
            }
        }
    }

    @EventHandler
    fun onFormResponse(e: PlayerFormRespondedEvent) {
        val p = e.player

        val data = arena.getPlayerData(p) ?: return
        val response = e.response as? FormResponseSimple ?: return

        this.arena.plugin.shopFormManager.handleResponse(p, data, e.formID, response)
    }
}