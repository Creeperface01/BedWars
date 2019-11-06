package com.creeperface.nukkit.bedwars.arena.listener

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
import cn.nukkit.event.inventory.CraftItemEvent
import cn.nukkit.event.inventory.InventoryClickEvent
import cn.nukkit.event.inventory.InventoryPickupArrowEvent
import cn.nukkit.event.inventory.InventoryTransactionEvent
import cn.nukkit.event.player.*
import cn.nukkit.inventory.transaction.action.SlotChangeAction
import cn.nukkit.item.Item
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.network.protocol.EntityEventPacket
import cn.nukkit.utils.MainLogger
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityMine
import com.creeperface.nukkit.bedwars.entity.TNTShip
import com.creeperface.nukkit.bedwars.entity.Villager
import com.creeperface.nukkit.bedwars.mysql.Stat
import com.creeperface.nukkit.bedwars.obj.BedWarsData
import com.creeperface.nukkit.bedwars.shop.ItemWindow
import com.creeperface.nukkit.bedwars.shop.ShopWindow
import com.creeperface.nukkit.bedwars.shop.Window
import com.creeperface.nukkit.bedwars.utils.BedWarsExplosion
import com.creeperface.nukkit.bedwars.utils.Items
import com.creeperface.nukkit.bedwars.utils.Lang
import java.util.*

class ArenaListener(private val arena: Arena) : Listener {

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

        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && arena.inArena(p)) {
            val team = arena.isJoinSign(e.block)

            if (team != 0) {
                arena.addToTeam(p, team)
                return
            }
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

        if (this.arena.game == Arena.ArenaState.LOBBY && e.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && e.item.id == Item.CLOCK) {
            this.arena.leaveArena(p)
            return
        }

        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && b.id == Block.ENDER_CHEST) {
            e.setCancelled()
            p.addWindow(data.team.enderChest)
        }

        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && item.id == Item.SPAWN_EGG && item.damage == TNTShip.NETWORK_ID) {

            val nbt = CompoundTag()
                    .putList(ListTag<DoubleTag>("Pos")
                            .add(DoubleTag("", b.getX() + 0.5))
                            .add(DoubleTag("", b.getY() + 1))
                            .add(DoubleTag("", b.getZ() + 0.5)))
                    .putList(ListTag<DoubleTag>("Motion")
                            .add(DoubleTag("", 0.toDouble()))
                            .add(DoubleTag("", 0.toDouble()))
                            .add(DoubleTag("", 0.toDouble())))
                    .putList(ListTag<FloatTag>("Rotation")
                            .add(FloatTag("", Random().nextFloat() * 360))
                            .add(FloatTag("", 0.toFloat())))

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

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        e.drops = arrayOfNulls(0)
        MainLogger.getLogger().logException(NullPointerException("bedwars death"))
    }

    @EventHandler
    fun onDropItem(e: PlayerDropItemEvent) {
        val p = e.player

        if (!p.isOp && this.arena.game == Arena.ArenaState.LOBBY) {
            e.setCancelled()
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onHit(e: EntityDamageEvent) {
        val victim = e.entity

        if (arena.game == Arena.ArenaState.LOBBY || victim.level.id != this.arena.level.id) {
            if (victim is Player) {
                if (this.arena.game == Arena.ArenaState.LOBBY && e.cause == EntityDamageEvent.DamageCause.VOID && arena.inArena(victim)) {
                    victim.teleport(this.arena.lobby)
                    e.setCancelled()
                }
            }

            return
        }

        var kill = false

        var data: BedWarsData? = null
        var kData: BedWarsData

        if (victim is Player) {
            if (arena.isSpectator(victim)) {
                e.setCancelled()
                return
            }

            if (!this.arena.inArena(victim)) {
                return
            }

            if (this.arena.game == Arena.ArenaState.LOBBY) {
                e.setCancelled()
                return
            }

            if (victim.getGamemode() == 3) {
                e.setCancelled()

                if (e.cause == EntityDamageEvent.DamageCause.VOID) {
                    victim.setMotion(Vector3(0.0, 10.0, 0.0))
                }
            }

            if (victim.getHealth() - e.finalDamage < 1) {
                kill = true
                data = arena.playerData[victim.getName().toLowerCase()]
            }
        } else if (victim.networkId == Villager.NETWORK_ID) {
            e.setCancelled()
        }

        if (e is EntityDamageByEntityEvent) {
            if (e.damager is Player) {
                val killer = e.damager as Player

                if (!arena.inArena(killer)) {
                    return
                }

                if (victim is Player) {
                    if (data == null) {
                        data = arena.playerData[victim.getName().toLowerCase()]
                    }
                    kData = arena.playerData[killer.name.toLowerCase()]!!

                    if (this.arena.game == Arena.ArenaState.LOBBY || data!!.team.id == kData.team.id) {
                        e.setCancelled()
                        return
                    }

                    if (!kill) {
                        data.lastHit = System.currentTimeMillis()
                        data.killer = killer.name
                        data.killerColor = "" + kData.team.chatColor
                    }
                }

                if (e !is EntityDamageByChildEntityEvent && victim.networkId == Villager.NETWORK_ID && killer.getGamemode() == 0) {
                    kData = arena.playerData[killer.name.toLowerCase()]!!
                    val inv = kData.team.shop

                    val id = killer.getWindowId(inv)

                    if (id >= 0) {
                        inv.onOpen(killer)
                    } else {
                        killer.addWindow(inv)
                    }

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

            this.arena.deathManager.onDeath(event)

            data!!.add(Stat.DEATHS)

            p.inventory.clearAll()

            if (!data.canRespawn()) {
                this.arena.unsetPlayer(p)
                p.sendMessage(BedWars.prefix + (Lang.JOIN_SPECTATOR.translate()))
                this.arena.setSpectator(p, true)

//                data.baseData.addExp(200) //played

                arena.checkAlive()
            } else {
                p.teleport(data.team.mapConfig.spawn)
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(e: BlockBreakEvent) {
        val p = e.player
        val b = e.block

        val data = arena.getPlayerData(p) ?: return

        if (e.isFastBreak || b.id != Block.BED_BLOCK && !Arena.allowedBlocks.contains(b.id)) {
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

        if (this.arena.game == Arena.ArenaState.LOBBY) {
            e.setCancelled()
            return
        }

//        data.baseData.addExp(1)

        val isBed = arena.isBed(b)

        if (isBed != null/* && NukerCheck.run(p, b)*/) {
            e.isCancelled = !this.arena.onBedBreak(p, isBed, b)
            e.drops = arrayOfNulls(0)
            return
        }

        if (b.id == Item.SPONGE) {
            val randomItem = Items.luckyBlock

            if (TextFormat.clean(randomItem.customName).startsWith("Legendary")) {
                arena.messageAllPlayers(Lang.LEGEND_FOUND, data.team.chatColor.toString() + p.name, randomItem.customName)
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

        if (e.isCancelled || !Arena.allowedBlocks.contains(b.id)) {
            e.setCancelled()
            return
        }

        val data = arena.getPlayerData(p) ?: return

        if (this.arena.isSpectator(p)) {
            e.setCancelled()
            return
        }

        if (this.arena.game == Arena.ArenaState.LOBBY) {
            e.setCancelled()
            return
        }

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

    @EventHandler
    fun onArrowPickup(e: InventoryPickupArrowEvent) {
        e.setCancelled()
    }

    @EventHandler
    fun onBucketFill(e: PlayerBucketFillEvent) {
        val p = e.player
        if (!p.isOp || this.arena.inArena(p)) {
            e.setCancelled()
        }
    }

    @EventHandler
    fun onBucketEmpty(e: PlayerBucketEmptyEvent) {
        val p = e.player
        if (!p.isOp || this.arena.inArena(p)) {
            e.setCancelled()
        }
    }

    @EventHandler
    fun onCraft(e: CraftItemEvent) {
        e.setCancelled()
    }

    @EventHandler
    fun onBedEnter(e: PlayerBedEnterEvent) {
        e.setCancelled()
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onProjectileHit(e: ProjectileHitEvent) {
        val ent = e.entity

        if (arena.game == Arena.ArenaState.LOBBY || ent.getLevel().id != arena.level.id) {
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

        if (arena.spectators.containsKey(p.name.toLowerCase())) {
            val msg = TextFormat.GRAY.toString() + "[" + TextFormat.BLUE + "SPECTATE" + TextFormat.GRAY + "] " + TextFormat.RESET + TextFormat.WHITE + p.displayName + TextFormat.GRAY + " > " /*+ data2.chatColor*/ + e.message //TODO: chat color

            for (s in arena.spectators.values) {
                s.sendMessage(msg)
            }

            e.setCancelled()
            return
        }

        val data = arena.getPlayerData(p) ?: return

        if (e.message.startsWith("!") && e.message.length > 1) {
            this.arena.messageAllPlayers(e.message, p, data)
        } else {
            data.team.messagePlayers(e.message, data)
        }
    }

    @EventHandler
    fun onTransaction(e: InventoryTransactionEvent) {
        val transaction = e.transaction

        for (action in transaction.actions) {
            if (action !is SlotChangeAction) {
                continue
            }

            if (action.inventory is Window) {
                e.setCancelled()
                return
            }
        }
    }

    @EventHandler
    fun onSlotClick(e: InventoryClickEvent) {
        val p = e.player

        val inv = p.inventory
        val inv2: Window
        if (e.inventory is Window) {
            inv2 = e.inventory as Window
        } else {
            return
        }

        e.setCancelled()
        val slot = e.slot

        if (!arena.inArena(p)) {
            return
        }

        if (inv2 is ShopWindow) {
            if (slot == 0) {
                val cost = inv2.cost
                val item = inv2.item

                if (!Items.containsItem(inv, cost)) {
                    p.sendMessage(Lang.LOW_SHOP.translate(cost.customName))
                    return
                }

                if (!inv.canAddItem(item)) {
                    p.sendMessage(Lang.FULL_INVENTORY.translate())
                    return
                }

                Items.removeItem(inv, cost)
                inv.addItem(item)

                p.sendMessage(BedWars.prefix + (Lang.BUY.translate(if (item.hasCustomName()) item.customName else item.name)))
            } else {
                val window = inv2.getWindow(slot)

                window?.run {
                    p.addWindow(this)
                    onOpen(p)
                }
            }
        } else if (inv2 is ItemWindow) {
            val newWindow = inv2.getWindow(slot)

            if (newWindow != null) {
                val id = p.addWindow(newWindow)

                if (id >= 0) {
                    newWindow.onOpen(p)
                }
            }
        }
    }

    @EventHandler
    fun onHungerChange(e: PlayerFoodLevelChangeEvent) {
        if (e.isCancelled) {
            return
        }

        val p = e.player

        if (arena.inArena(p) && this.arena.game == Arena.ArenaState.LOBBY) {
            e.setCancelled()
        }
    }

    @EventHandler
    fun onFireSpread(e: BlockBurnEvent) {
        e.setCancelled()
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
    fun launchProjectile(e: ProjectileLaunchEvent) {
        val p = e.entity.shootingEntity

        if (p is Player && p.gamemode > 1) {
            e.setCancelled()
        }
    }
}