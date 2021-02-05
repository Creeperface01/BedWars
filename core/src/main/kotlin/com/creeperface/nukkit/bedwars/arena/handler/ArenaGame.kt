package com.creeperface.nukkit.bedwars.arena.handler

import cn.nukkit.AdventureSettings
import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.block.BlockAir
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityBed
import cn.nukkit.entity.Entity
import cn.nukkit.entity.item.EntityItem
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.event.player.PlayerTeleportEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemClock
import cn.nukkit.level.Level
import cn.nukkit.level.Position
import cn.nukkit.level.Sound
import cn.nukkit.level.particle.HugeExplodeSeedParticle
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.*
import cn.nukkit.network.protocol.InventoryContentPacket
import cn.nukkit.network.protocol.LevelEventPacket
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.arena.handler.GameHandler
import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.api.event.ArenaBedDestroyEvent
import com.creeperface.nukkit.bedwars.api.event.ArenaStartEvent
import com.creeperface.nukkit.bedwars.api.event.ArenaStopEvent
import com.creeperface.nukkit.bedwars.api.utils.BedWarsExplosion
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.api.utils.invoke
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.arena.ArenaState
import com.creeperface.nukkit.bedwars.arena.IArena
import com.creeperface.nukkit.bedwars.arena.Team
import com.creeperface.nukkit.bedwars.arena.handler.listener.ArenaGameListener
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityMine
import com.creeperface.nukkit.bedwars.entity.BWVillager
import com.creeperface.nukkit.bedwars.entity.SpecialItem
import com.creeperface.nukkit.bedwars.obj.BedWarsData
import com.creeperface.nukkit.bedwars.utils.*
import com.creeperface.nukkit.placeholderapi.api.scope.MessageScope
import com.creeperface.nukkit.placeholderapi.api.util.translatePlaceholders
import java.util.*

@Suppress("LeakingThis")
open class ArenaGame(
    val arena: Arena,
    override val teams: List<Team>,
    val playerData: MutableMap<String, BedWarsData>,
    override val mapConfig: MapConfiguration,
    override val level: Level,
) : IArena by arena, GameHandler {

    val gameSpectators = mutableMapOf<String, Player>()

    override val spectators: Map<String, Player>
        get() = gameSpectators.toMap()

    val listener = ArenaGameListener(this)

    override val state = ArenaState.GAME

    override var ending = false

    override var winner: Team? = null

    override val aliveTeams: List<Team>
        get() = this.teams.filter { it.isAlive() }

    override var closed = false

    init {
        val plugin = this.arena.plugin
        plugin.server.pluginManager.registerEvents(this.listener, plugin)
    }

    override fun getPlayerData(p: Player) = playerData[p.name.toLowerCase()]

    override fun getTeam(id: Int) = teams.getOrNull(id)

    override fun getPlayerTeam(p: Player) = getPlayerData(p)?.team

    override fun tryJoinPlayer(p: Player, message: Boolean, action: IArena.() -> Unit): Boolean {
        if (!configuration.allowSpectators) {
            message {
                p.sendMessage(Lang.GAME_IN_PROGRESS.translatePrefix())
            }
            return false
        }

        return arena.tryJoinPlayer(p, message, action)
    }

    override fun joinToArena(p: Player): Boolean {
        p.sendMessage(Lang.JOIN_SPECTATOR.translatePrefix())
        this.setSpectator(p)
        scoreboardManager.addPlayer(p)
        return true
    }

    fun start() {
        plugin.server.pluginManager.callEvent(ArenaStartEvent(plugin, this))

        this.task.startTime = this.startTime

        demo {
            if (gamesCount > 0) {
                return
            }
        }

//        val levelName = this.map + "_" + this.name
//        this.plugin.server.loadLevel(levelName)
//        this.level = this.plugin.server.getLevelByName(levelName)
        this.level.isRaining = false
        this.level.isThundering = false
        gamesCount++

        spawnBeds()
        spawnVillagers()

        this.level.time = 0
        this.level.stopTime()

//        for (data in this.playerData.values) {
//            if (!data.hasTeam()) {
//                this.selectTeam(data)
//            }
//        }

        this.signManager.updateMainSign()

        for (data in playerData.values) {
            val p = data.player

            val d = data.team.spawn

            p.teleport(Position.fromObject(d, this.level), PlayerTeleportEvent.TeleportCause.PLUGIN)
            this.level.addSound(p.add(0.toDouble(), p.eyeHeight.toDouble()), Sound.RANDOM_ANVIL_USE, 1f, 1f, p)

            p.inventory.clearAll()
            p.setExperience(0, 0)
            p.health = 20f
            p.setSpawn(p.temporalVector.setComponents(d.x, d.y + 2, d.z))
        }

        this.messageAllPlayers(Lang.START_GAME, false)

        scoreboardManager.initGame(this)
    }

    fun stop(cause: ArenaStopEvent.Cause = ArenaStopEvent.Cause.CUSTOM) {
        plugin.server.pluginManager.callEvent(ArenaStopEvent(plugin, this, winner, cause))

        this.unsetAllPlayers()
        this.task.reset()
        this.popupTask.ending = endingTime

        demo {
            logAlert("Continuous game count is limited to 1 in demo mode. Restart the server to start a new game")
            listener.unregisterAll()
            plugin.commandListener.unregisterAll()
            plugin.listener.unregisterAll()
        }

        demo {
            task.cancel()
            popupTask.cancel()
        }

        this.level.unload()

        signManager.updateMainSign()
        scoreboardManager.reset()

        notDemo {
            initHandler()
        }
    }

    fun onBedBreak(p: Player, bedteam: Team, b: Block): Boolean {
        val data = getPlayerData(p) ?: return false
        val pTeam = data.team

        if (pTeam.id == bedteam.id) {
            p.sendMessage(Lang.BREAK_OWN_BED.translatePrefix())
            return false
        }

        if (!bedteam.hasBed()) {
            return false
        }

        val ev = ArenaBedDestroyEvent(plugin, this, data, bedteam)
        plugin.server.pluginManager.callEvent(ev)

        if (ev.isCancelled) {
            return false
        }

        for (pl in bedteam.players.values) {
            if (p.isOnline) {
                pl.player.setSpawn(this.plugin.server.defaultLevel.spawnLocation)
            }
        }

        data.addStat(Stat.BEDS)
//        data.baseData.addShard(5)

        this.level.addParticle(HugeExplodeSeedParticle(Vector3(b.x, b.y, b.z)))
        val pk = LevelEventPacket()
        pk.evid = LevelEventPacket.EVENT_SOUND_EXPLODE
        pk.data = 0

        for (data2 in playerData.values) {
            pk.x = data2.player.x.toInt().toFloat()
            pk.y = data2.player.y.toInt() + data2.player.eyeHeight
            pk.z = data2.player.z.toInt().toFloat()
            data2.player.dataPacket(pk)
        }

        for (player in gameSpectators.values) {
            pk.x = player.x.toInt().toFloat()
            pk.y = player.y.toInt() + player.eyeHeight
            pk.z = player.z.toInt().toFloat()
            player.dataPacket(pk)
        }

        val team = data.team
        val color = "" + team.chatColor
        val name = team.name

        messageAllPlayers(
            Lang.BED_BREAK,
            false,
            "" + bedteam.chatColor,
            color + p.name,
            color + name,
            bedteam.chatColor.toString() + bedteam.name
        )
        bedteam.onBedBreak()

        checkAlive()
        return true
    }

    fun isBed(b: Block): Team? {
        if (b.id != Item.BED_BLOCK) {
            return null
        }

        teams.forEach {
            if (b == it.bed1 || b == it.bed2) {
                return it
            }
        }

        return null
    }

    fun checkAlive() {
        if (!this.ending) {
            val aliveTeams = this.aliveTeams

            if (aliveTeams.size == 1) {
                val team = aliveTeams[0]
                winner = team

                for (pl in team.players.values) {
                    pl.addStat(Stat.WINS)
                }

                messageAllPlayers(Lang.END_GAME, false, "" + team.chatColor, team.name)
                this.ending = true
            }

        }
    }

    override fun unsetPlayer(p: Player) {
        arena.unsetPlayer(p)

        playerData.remove(p.name.toLowerCase())?.let { data ->
            data.team.removePlayer(data)
        }
    }

    override fun dropBronze() {
        this.mapConfig.bronze.forEach { vec ->
            this.dropItem(vec, Items.BRONZE)
        }
    }

    override fun dropIron() {
        this.mapConfig.iron.forEach { vec ->
            this.dropItem(vec, Items.IRON)
        }
    }

    override fun dropGold() {
        this.mapConfig.gold.forEach { vec ->
            this.dropItem(vec, Items.GOLD)
        }
    }

    fun dropItem(v: Vector3, item: Item) {
        val motion = Vector3(0.0, 0.2, 0.0)
        val itemTag = NBTIO.putItemHelper(item)
        itemTag.name = "Item"

        val entities =
            this.level.getNearbyEntities(SimpleAxisAlignedBB(v.x - 1, v.y - 1, v.z - 1, v.x + 1, v.y + 1, v.z + 1))

        for (entity in entities) {
            if (entity is EntityItem) {

                if (!entity.closed && entity.isAlive && entity.item.count < 64 && entity.item.equals(
                        item,
                        true,
                        false
                    )
                ) {
                    entity.item.count++
                    return
                }
            }
        }

        val itemEntity = SpecialItem(
            this.level.getChunk(v.getX().toInt() shr 4, v.getZ().toInt() shr 4, true),
            CompoundTag().putList(
                ListTag<Tag>("Pos").add(DoubleTag("", v.getX() + 0.5)).add(DoubleTag("", v.getY()))
                    .add(DoubleTag("", v.getZ() + 0.5))
            ).putList(
                ListTag<Tag>("Motion").add(DoubleTag("", motion.x)).add(DoubleTag("", motion.y))
                    .add(DoubleTag("", motion.z))
            ).putList(ListTag<Tag>("Rotation").add(FloatTag("", Random().nextFloat() * 360.0f)).add(FloatTag("", 0.0f)))
                .putShort("Health", 5).putCompound("Item", itemTag).putShort("PickupDelay", 0)
        )

        if (item.id > 0 && item.getCount() > 0) {
            itemEntity.spawnToAll()
        }
    }

    override fun messageGamePlayers(lang: Lang, vararg args: String) {
        messageGamePlayers(lang, false, *args)
    }

    override fun messageGamePlayers(lang: Lang, addPrefix: Boolean, vararg args: String) {
        val translation = lang.translate(*args)

        arenaPlayers.values.forEach { it.sendMessage(if (addPrefix) BedWars.chatPrefix else "" + translation) }
    }

    override fun messageAllPlayers(lang: Lang, addPrefix: Boolean, vararg args: String) {
        arena.messageAllPlayers(lang, addPrefix, *args)

        val translation = lang.translate(*args)
        gameSpectators.values.forEach { it.sendMessage(if (addPrefix) BedWars.chatPrefix else "" + translation) }
    }

    override fun messageAllPlayers(message: String, player: Player, data: BedWarsData?) {
        val pData = data ?: getPlayerData(player) ?: return

        val msg = configuration.allFormat.translatePlaceholders(
            player,
            context,
            pData.team.context,
            MessageScope.getContext(player, message)
        )

        arenaPlayers.values.forEach {
            it.sendMessage(msg)
        }

        gameSpectators.values.forEach {
            it.sendMessage(msg)
        }

        plugin.server.logger.info(msg)
    }

    override fun isSpectator(p: Player): Boolean {
        return this.gameSpectators.containsKey(p.name.toLowerCase())
    }

    override fun setSpectator(p: Player) = setSpectator(p, false)

    fun setSpectator(p: Player, respawn: Boolean) {
        if (playerData.isEmpty() || isSpectator(p)) {
            return
        }

        var tpPos: Position = p.clone()

        unsetPlayer(p) //for sure

        if (!respawn) {
            tpPos = this.playerData[playerData.keys.random()]!!.player
        }

        if (tpPos.y < 10) {
            tpPos.y = 10.0
        }

        this.gameSpectators[p.name.toLowerCase()] = p

        p.isSneaking = false
        p.inventory.clearAll()

        p.inventory.setItem(5, ItemClock().setCustomName("" + TF.ITALIC + TF.AQUA + "Lobby"))
        p.inventory.sendContents(p)
        /**
         * Special method for spectator mode change
         */

        p.gamemode = 3

        p.adventureSettings.set(AdventureSettings.Type.ALLOW_FLIGHT, true)
        p.adventureSettings.set(AdventureSettings.Type.FLYING, true)
        p.adventureSettings.set(AdventureSettings.Type.WORLD_BUILDER, false)
        p.adventureSettings.set(AdventureSettings.Type.WORLD_IMMUTABLE, true)
        p.adventureSettings.set(AdventureSettings.Type.NO_CLIP, true)
        p.adventureSettings.update()
        p.despawnFromAll()

        val inventoryContentPacket = InventoryContentPacket()
        inventoryContentPacket.inventoryId = InventoryContentPacket.SPECIAL_CREATIVE
        p.dataPacket(inventoryContentPacket)

        p.inventory.sendContents(p)
        p.nameTag = p.name

        this.scoreboardManager.addPlayer(p)
        p.teleport(tpPos)
    }

    override fun leaveArena(p: Player) {
        if (isSpectator(p)) {
            unsetSpectator(p)
            return
        }

        val data = getPlayerData(p) ?: return
        data.team.messagePlayers(Lang.PLAYER_LEAVE.translate(data.team.chatColor + p.name))
        data.addStat(Stat.LOSSES)

        scoreboardManager.updateTeam(this, data.team.id)

        arena.leaveArena(p)

        this.checkAlive()
    }

    fun unsetSpectator(p: Player) {
        this.gameSpectators.remove(p.name.toLowerCase())
        this.scoreboardManager.removePlayer(p)

        p.adventureSettings.set(AdventureSettings.Type.ALLOW_FLIGHT, false)
        p.adventureSettings.set(AdventureSettings.Type.FLYING, false)
        p.adventureSettings.update()
    }

    fun onEntityInteract(e: PlayerInteractEvent) {
        val p = e.player
        val b = e.block

        if (e.isCancelled) {
            return
        }

        if (e.action != PlayerInteractEvent.Action.PHYSICAL || b.level.id != this.level.id || b.id != Block.STONE_PRESSURE_PLATE) {
            return
        }

        val blockEntity = b.level.getBlockEntity(b) as? BlockEntityMine ?: return

        val data = getPlayerData(p)

        if (data?.team == null) {
            e.setCancelled()
            return
        }

        if (blockEntity.getTeam() == data.team.id) {
            e.setCancelled()
            return
        }

        e.setCancelled()
        BedWarsExplosion(b.add(0.5, 0.5, 0.5), 0.8, null).explode(this, blockEntity.getTeam())
        b.level.setBlock(b, BlockAir(), true, false)
    }

    override fun unsetAllPlayers() {
        arena.unsetAllPlayers()

        this.gameSpectators.values.toList().forEach { this.unsetSpectator(it) }
        this.gameSpectators.clear()
    }

    private fun spawnBeds() {
        for (team in teams) {
            val positions = arrayOf(team.bed1, team.bed2)

            for (pos in positions) {
                while (true) {
                    val be = this.level.getBlockEntity(pos) ?: break

                    be.close()
                }

                val nbt = BlockEntity.getDefaultCompound(pos, BlockEntity.BED)
                nbt.putByte("color", team.color.woolData)

                BlockEntityBed(this.level.getChunk(pos.chunkX, pos.chunkZ), nbt)
            }
        }
    }

    private fun spawnVillagers() {
        this.teams.forEach {
            val bb = SimpleAxisAlignedBB(
                it.villager.floor(),
                it.villager.floor().add(1.0, 1.0, 1.0)
            )

            val pos = it.villager.floor().add(0.5, 0.0, 0.5)
            val chunk = level.getChunk(pos.chunkX, pos.chunkZ)

            chunk.entities.values.forEach { ent ->
                if (bb.isVectorInside(ent)) {
                    ent.close()
                }
            }

            BWVillager(
                chunk,
                Entity.getDefaultNBT(pos)
            )
        }
    }

    override fun close() {
        super.close()

        this.listener.unregisterAll()
    }
}