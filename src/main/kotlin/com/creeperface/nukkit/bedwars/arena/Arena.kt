package com.creeperface.nukkit.bedwars.arena

import cn.nukkit.AdventureSettings.Type
import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.block.Block
import cn.nukkit.block.BlockAir
import cn.nukkit.block.BlockID
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityBed
import cn.nukkit.entity.item.EntityItem
import cn.nukkit.entity.projectile.EntityArrow
import cn.nukkit.event.EventHandler
import cn.nukkit.event.EventPriority
import cn.nukkit.event.Listener
import cn.nukkit.event.block.BlockBreakEvent
import cn.nukkit.event.block.BlockBurnEvent
import cn.nukkit.event.block.BlockPlaceEvent
import cn.nukkit.event.entity.*
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.event.inventory.*
import cn.nukkit.event.player.*
import cn.nukkit.event.player.PlayerInteractEvent.Action
import cn.nukkit.inventory.transaction.action.SlotChangeAction
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
import cn.nukkit.network.protocol.EntityEventPacket
import cn.nukkit.network.protocol.InventoryContentPacket
import cn.nukkit.network.protocol.LevelEventPacket
import cn.nukkit.utils.MainLogger
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.arena.config.ArenaConfiguration
import com.creeperface.nukkit.bedwars.arena.config.IArenaConfiguration
import com.creeperface.nukkit.bedwars.arena.config.MapConfiguration
import com.creeperface.nukkit.bedwars.arena.manager.DeathManager
import com.creeperface.nukkit.bedwars.arena.manager.ScoreboardManager
import com.creeperface.nukkit.bedwars.arena.manager.SignManager
import com.creeperface.nukkit.bedwars.arena.manager.VotingManager
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityMine
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityTeamSign
import com.creeperface.nukkit.bedwars.entity.SpecialItem
import com.creeperface.nukkit.bedwars.entity.TNTShip
import com.creeperface.nukkit.bedwars.entity.Villager
import com.creeperface.nukkit.bedwars.mysql.Stat
import com.creeperface.nukkit.bedwars.obj.BedWarsData
import com.creeperface.nukkit.bedwars.obj.Language
import com.creeperface.nukkit.bedwars.obj.Team
import com.creeperface.nukkit.bedwars.shop.ItemWindow
import com.creeperface.nukkit.bedwars.shop.ShopWindow
import com.creeperface.nukkit.bedwars.shop.Window
import com.creeperface.nukkit.bedwars.task.WorldCopyTask
import com.creeperface.nukkit.bedwars.utils.*
import java.util.*
import kotlin.collections.ArrayList

class Arena(var plugin: BedWars, val config: ArenaConfiguration) : Listener, IArenaConfiguration by config {

    val playerData = mutableMapOf<String, BedWarsData>()
    val spectators = mutableMapOf<String, Player>()

    val teams = ArrayList<Team>(config.teamData.size)

    private val task = ArenaSchedule(this)
    private val popupTask = PopupTask(this)

    internal val votingManager: VotingManager
    internal val scoreabordManager = ScoreboardManager(this)
    private val deathManager: DeathManager
    private val signManager = SignManager(this)

    var map = "Voting"
    var winnerTeam: Int = 0
    internal var canJoin = true
    var isLevelLoaded = false

    var game = ArenaState.LOBBY
    var starting = false
    var ending = false

    lateinit var level: Level
    lateinit var mapConfig: MapConfiguration

    private val aliveTeams: ArrayList<Team>
        get() {
            val teams = ArrayList<Team>()

            for (team in this.teams) {
                if (team.hasBed() || team.players.isNotEmpty()) {
                    teams.add(team)
                }
            }

            return teams
        }

    init {
        this.enableScheduler()
        this.votingManager = VotingManager(this)
        this.deathManager = DeathManager(this)
        this.votingManager.createVoteTable()
        scoreabordManager.initVotes()
        initTeams()
        signManager.init()

        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun initTeams() {
        teams.clear()
        teams.addAll(config.teamData.mapIndexed { index, conf -> Team(this, index, conf) })
    }

    private fun enableScheduler() {
        this.plugin.server.scheduler.scheduleRepeatingTask(this.task, 20)
        this.plugin.server.scheduler.scheduleRepeatingTask(this.popupTask, 20)
    }

    @EventHandler
    fun onBlockTouch2(e: PlayerInteractEvent) {
        val p = e.player

        if (isSpectator(p)) {
            if (e.action == Action.RIGHT_CLICK_AIR && e.item.id == Item.CLOCK) {
                this.leaveArena(p)
                return
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onBlockTouch(e: PlayerInteractEvent) {
        val b = e.block
        val p = e.player
        val item = e.item

        if (e.action == Action.PHYSICAL) {
            onEntityInteract(e)
            return
        }

//        if (e.action == Action.RIGHT_CLICK_BLOCK ) { //TODO: main sign click
//            val be = b.blockEntity
//
//            if(be is BlockEntityTeamSign) {
//                e.setCancelled()
//
//                if (plugin.getPlayerArena(p) != null)
//                    return
//
//                if (!this.multiPlatform && p.loginChainData.deviceOS == 7 && !p.hasPermission("gameteam.helper") && !p.hasPermission("gameteam.mcpe")) {
//                    p.sendMessage(Lang.translate("pe_only", p))
//                    return
//                }
//
//                this.joinToArena(p)
//                return
//            }
//        }

//        if (data1.isInLobby) {
//            e.setCancelled()
//            return
//        }

//        if (e.isCancelled) {
//            return
//        }

        val data = getPlayerData(p) ?: return

        if (this.game == ArenaState.LOBBY && e.action == Action.RIGHT_CLICK_AIR && e.item.id == Item.CLOCK) {
            this.leaveArena(p)
            return
        }

        val team = this.isJoinSign(b)

        if (team != 0) {
            this.addToTeam(p, team)
            return
        }

        if (e.action == Action.RIGHT_CLICK_BLOCK && b.id == Block.ENDER_CHEST) {
            e.setCancelled()
            p.addWindow(data.team.enderChest)
        }

        if (e.action == Action.RIGHT_CLICK_BLOCK && item.id == Item.SPAWN_EGG && item.damage == TNTShip.NETWORK_ID) {

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

            val ship = TNTShip(b.getLevel().getChunk(b.floorX shr 4, b.floorZ shr 4), nbt, this, data.team)
            ship.spawnToAll()

            item.count--
            p.inventory.itemInHand = item
            e.isCancelled = true
        }
    }

    fun messageAlivePlayers(msg: String) {
        for (data in ArrayList(this.playerData.values)) {
            if (data.player.isOnline) {
                data.player.sendMessage(BedWars.prefix + msg)
            }
        }
        this.plugin.server.logger.info(BedWars.prefix + msg)
    }

    fun joinToArena(p: Player) {
        if (this.game == ArenaState.GAME) {
            p.sendMessage(BedWars.prefix + Language.translate("join_spectator"))
            this.setSpectator(p)
            scoreabordManager.addPlayer(p)
            return
        }

        if (this.playerData.size >= 16 && !p.hasPermission("gameteam.vip")) {
            p.sendMessage(BedWars.prefix + Language.translate("game_full"))
            return
        }

        if (!this.canJoin) {
            return
        }

        val data = plugin.players[p.id]!!
        data.arena = this

        val pl = BedWarsData(this, p, data)
        playerData[p.name.toLowerCase()] = pl

        p.nameTag = p.name
        p.sendMessage(BedWars.prefix + Language.translate("join", this.name))
        p.teleport(this.lobby)
        scoreabordManager.addPlayer(p)
        p.setSpawn(this.lobby)

        val inv = p.inventory
        inv.clearAll()

        var i = 0
        while (i++ < teams.size) {
            val team = teams[i]

            inv.setItem(i, Item.get(Item.STAINED_TERRACOTTA, team.color.woolData, 1).setCustomName("§r§7Join " + team.chatColor + team.name)) //TODO: translate
        }

        inv.setItem(i, ItemClock().setCustomName("" + TextFormat.ITALIC + TextFormat.AQUA + "Lobby"))

        inv.sendContents(p)

        p.adventureSettings.set(Type.ALLOW_FLIGHT, false)
        p.gamemode = 3
        p.setGamemode(0)

        this.checkLobby()
        this.signManager.updateMainSign()
    }

    fun leaveArena(p: Player) {
        if (isSpectator(p)) {
            unsetSpectator(p)
            return
        }

        val data = getPlayerData(p)

        data?.team?.let {
            val pTeam = data.team

            if (this.game == ArenaState.GAME) {
                pTeam.messagePlayers(Language.translate("player_leave", pTeam.chatColor + p.name))
                data.add(Stat.LOSSES)
            }

            if (p.isOnline) {
                p.sendMessage(BedWars.prefix + Language.translate("leave"))
            }

            signManager.updateTeamSigns()
        }

        this.unsetPlayer(p)

        if (this.game == ArenaState.GAME) {
            this.checkAlive()
        }

        data?.globalData?.arena = null

        signManager.updateMainSign()
    }

    fun startGame() {
        if (!isLevelLoaded) {
            return
        }

        this.task.startTime = 50
        this.starting = false
        isLevelLoaded = false

        this.plugin.server.loadLevel(this.map + "_" + this.name)
        this.level = this.plugin.server.getLevelByName(this.map + "_" + name)
        this.level.isRaining = false
        this.level.isThundering = false

        for (team in teams) {
            val positions = arrayOf(team.mapConfig.bed1, team.mapConfig.bed2)

            for (pos in positions) {
                val nbt = BlockEntity.getDefaultCompound(pos, BlockEntity.BED)
                nbt.putByte("color", team.color.woolData)

                BlockEntityBed(this.level.getChunk(pos.floorX shr 4, pos.floorZ shr 4), nbt)
            }
        }

        this.level.time = 0
        this.level.stopTime()

        for (data in this.playerData.values) {
            if (!data.hasTeam()) {
                this.selectTeam(data)
            }
        }

        this.game = ArenaState.GAME
        this.signManager.updateMainSign()

        for (data in playerData.values) {
            val p = data.player

            val d = data.team.mapConfig.spawn

            p.teleport(Position(d.x, d.y, d.z, this.level), PlayerTeleportEvent.TeleportCause.PLUGIN)
            this.level.addSound(p.add(0.toDouble(), p.eyeHeight.toDouble()), Sound.RANDOM_ANVIL_USE, 1f, 1f, p)

            p.inventory.clearAll()
            p.setExperience(0, 0)
            p.health = 20f
            p.setSpawn(p.temporalVector.setComponents(d.x, d.y + 2, d.z))

            if (p.hasPermission("gameteam.vip")) {
                val bronze = Items.BRONZE.clone()
                bronze.setCount(16)
                val iron = Items.IRON.clone()
                iron.setCount(3)

                p.inventory.addItem(bronze.clone())
                p.inventory.addItem(iron.clone())
                p.inventory.addItem(Items.GOLD.clone())
            }
        }

        this.messageAllPlayers("start_game", false)

        scoreabordManager.initGame()
    }

    fun selectTeam(data: BedWarsData) {
        var teamm: Team? = null
        val p = data.player

        for (team in teams) {
            if (!isTeamFull(team) || isTeamFree(team) || p.hasPermission("gameteam.vip")) {
                teamm = team
            }
        }

        teamm?.addPlayer(data)
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        if (inArena(e.player) || this.isSpectator(e.player)) {
            this.leaveArena(e.player)
        }
    }

    private fun checkAlive() {
        if (!this.ending) {
            val aliveTeams = this.aliveTeams

            if (aliveTeams.size == 1) {
                val team = aliveTeams[0]
                winnerTeam = team.id

                for (pl in team.players.values) {
                    val p = pl.player
                    p.sendMessage(BedWars.prefix + TextFormat.GOLD + "Obdrzel jsi 10 tokenu a 500 xp za vyhru!")
                    pl.add(Stat.WINS)
//                    pl.baseData.addShard(10)
                }

                messageAllPlayers("end_game", false, "" + team.chatColor, team.name)
                this.ending = true
            }

        }

        if (this.playerData.isEmpty()) {
            Server.getInstance().scheduler.scheduleDelayedTask(plugin, { stopGame() }, 1)
        }
    }

    fun stopGame() {
        this.unsetAllPlayers()
        this.task.gameTime = 0
        this.task.startTime = 50
        this.task.drop = 0
        this.task.sign = 0
        this.popupTask.ending = 20
        this.votingManager.players.clear()
        this.votingManager.createVoteTable()
        scoreabordManager.initVotes()
        this.ending = false
        this.winnerTeam = -1
        this.game = ArenaState.LOBBY

        this.level.unload()

        signManager.updateMainSign()
        scoreabordManager.reset()
    }

    fun unsetAllPlayers() {
        ArrayList(this.playerData.values).forEach { unsetPlayer(it.player) }
        ArrayList(this.spectators.values).forEach { this.unsetSpectator(it) }

        this.spectators.clear()
        this.playerData.clear()
        this.resetTeams()
    }

    fun onRespawn(e: PlayerRespawnEvent) {
        val p = e.player
        if (!this.inArena(p)) {
            return
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

        if (!p.isOp && this.game == ArenaState.LOBBY) {
            e.setCancelled()
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onHit(e: EntityDamageEvent) {
        val victim = e.entity

        if (!::level.isInitialized || victim.getLevel().id != this.level.id) {
            if (victim is Player) {

                if (this.game == ArenaState.LOBBY && e.cause == DamageCause.VOID && inArena(victim)) {
                    victim.teleport(this.lobby)
                    e.setCancelled()
                }
            }

            return
        }

        var kill = false

        var data: BedWarsData? = null
        var kData: BedWarsData

        if (victim is Player) {
            if (isSpectator(victim)) {
                e.setCancelled()
                return
            }

            if (!this.inArena(victim)) {
                return
            }

            if (this.game == ArenaState.LOBBY) {
                e.setCancelled()
                return
            }

            if (victim.getGamemode() == 3) {
                e.setCancelled()

                if (e.cause == DamageCause.VOID) {
                    victim.setMotion(Vector3(0.0, 10.0, 0.0))
                }
            }

            if (victim.getHealth() - e.finalDamage < 1) {
                kill = true
                data = playerData[victim.getName().toLowerCase()]
            }
        } else if (victim.networkId == Villager.NETWORK_ID) {
            e.setCancelled()
        }

        if (e is EntityDamageByEntityEvent) {
            if (e.damager is Player) {
                val killer = e.damager as Player

                if (!inArena(killer)) {
                    return
                }

                if (victim is Player) {
                    if (data == null) {
                        data = playerData[victim.getName().toLowerCase()]
                    }
                    kData = playerData[killer.name.toLowerCase()]!!

                    if (this.game == ArenaState.LOBBY || data!!.team.id == kData.team.id) {
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
                    kData = playerData[killer.name.toLowerCase()]!!
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

            this.deathManager.onDeath(event)

            data!!.add(Stat.DEATHS)

            p.inventory.clearAll()

            if (!data.canRespawn()) {
                this.unsetPlayer(p)
                p.sendMessage(BedWars.prefix + Language.translate("join_spectator"))
                this.setSpectator(p, true)

//                data.baseData.addExp(200) //played

                checkAlive()
            } else {
                p.teleport(data.team.mapConfig.spawn)
            }
        }
    }

    private fun isJoinSign(b: Block): Int {
        return (b.blockEntity as? BlockEntityTeamSign)?.team ?: -1
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(e: BlockBreakEvent) {
        val p = e.player
        val b = e.block

        val data = getPlayerData(p) ?: return

        if (e.isFastBreak || b.id != Block.BED_BLOCK && !allowedBlocks.contains(b.id)) {
            e.setCancelled()
            return
        }

        if (b.id == Block.STONE_PRESSURE_PLATE) {
            e.drops = arrayOfNulls(0)
        }

        if (this.isSpectator(p)) {
            e.setCancelled()
            return
        }

        if (this.game == ArenaState.LOBBY) {
            e.setCancelled()
            return
        }

//        data.baseData.addExp(1)

        val isBed = isBed(b)

        if (isBed != null/* && NukerCheck.run(p, b)*/) {
            e.isCancelled = !this.onBedBreak(p, isBed, b)
            e.drops = arrayOfNulls(0)
            return
        }

        if (b.id == Item.SPONGE) {
            val randomItem = Items.luckyBlock

            if (TextFormat.clean(randomItem.customName).startsWith("Legendary")) {
                messageAllPlayers("legend_found", data.team.chatColor.toString() + p.name, randomItem.customName)
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

        if (e.isCancelled || !allowedBlocks.contains(b.id)) {
            e.setCancelled()
            return
        }

        val data = getPlayerData(p) ?: return

        if (this.isSpectator(p)) {
            e.setCancelled()
            return
        }

        if (this.game == ArenaState.LOBBY) {
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

    private fun onBedBreak(p: Player, bedteam: Team, b: Block): Boolean {
        val data = getPlayerData(p) ?: return false
        val pTeam = data.team

        if (pTeam.id == bedteam.id) {
            p.sendMessage(BedWars.prefix + Language.translate("break_own_bed"))
            return false
        }

        if (!bedteam.hasBed()) {
            return false
        }

        for (pl in ArrayList(bedteam.players.values)) {
            if (p.isOnline) {
                pl.player.setSpawn(this.plugin.mainLobby)
            }
        }

        data.add(Stat.BEDS)
//        data.baseData.addShard(5)

        this.level.addParticle(HugeExplodeSeedParticle(Vector3(b.x, b.y, b.z)))
        val pk = LevelEventPacket()
        pk.evid = LevelEventPacket.EVENT_SOUND_EXPLODE
        pk.data = 0

        for (data2 in ArrayList(playerData.values)) {
            pk.x = data2.player.x.toInt().toFloat()
            pk.y = data2.player.y.toInt() + data2.player.eyeHeight
            pk.z = data2.player.z.toInt().toFloat()
            data2.player.dataPacket(pk)
        }

        for (player in ArrayList(spectators.values)) {
            pk.x = player.x.toInt().toFloat()
            pk.y = player.y.toInt() + player.eyeHeight
            pk.z = player.z.toInt().toFloat()
            player.dataPacket(pk)
        }

        val team = data.team
        val color = "" + team.chatColor
        val name = team.name

        messageAllPlayers("bed_break", false, "" + bedteam.chatColor, color + p.name, color + name, bedteam.chatColor.toString() + bedteam.name)
        bedteam.onBedBreak()

        checkAlive()
        return true
    }

    private fun isBed(b: Block): Team? {
        if (b.id != Item.BED_BLOCK) {
            return null
        }

        teams.forEach {
            if (b == it.mapConfig.bed1 || b == it.mapConfig.bed2) {
                return it
            }
        }

        return null
    }

    fun getPlayerTeam(p: Player) = this.playerData[p.name.toLowerCase()]?.team

    fun getPlayerColor(p: Player) = this.playerData[p.name]?.team?.color?.rgb

    fun isTeamFree(team: Team): Boolean {
        val minPlayers = this.teams.filter { it !== team }.minBy { it.players.size }?.players?.size ?: team.players.size

        return team.players.size - minPlayers < 2
    }

    fun addToTeam(p: Player, team: Int) {
        val pTeam = teams[team]
        val data = playerData[p.name.toLowerCase()]!!

        if ((isTeamFull(pTeam) || !isTeamFree(pTeam)) && !p.hasPermission("gameteam.vip")) {
            p.sendMessage(BedWars.prefix + Language.translate("full_team"))
            return
        }

        val currentTeam = data.team

        if (currentTeam.id == pTeam.id) {
            p.sendMessage(BedWars.prefix + Language.translate("already_in_team", pTeam.chatColor.toString() + pTeam.name))
            return
        }

        if (currentTeam.id != 0) {
            currentTeam.removePlayer(data)
        }

        pTeam.addPlayer(data)

        signManager.updateTeamSigns()

        p.sendMessage(Language.translate("team_join", pTeam.chatColor.toString() + pTeam.name))
    }

    fun isTeamFull(team: Team): Boolean {
        return team.players.size >= 4
    }

    fun unsetPlayer(p: Player) {
        this.scoreabordManager.removePlayer(p)

        val data = playerData.remove(p.name.toLowerCase())

        if (data != null) {
            data.globalData.arena = null

            data.team.removePlayer(data)
        }
    }

    fun dropBronze() {
        this.mapConfig.bronze.forEach { vec ->
            this.dropItem(vec, Items.BRONZE)
        }
    }

    fun dropIron() {
        this.mapConfig.iron.forEach { vec ->
            this.dropItem(vec, Items.IRON)
        }
    }

    fun dropGold() {
        this.mapConfig.gold.forEach { vec ->
            this.dropItem(vec, Items.GOLD)
        }
    }

    private fun dropItem(v: Vector3, item: Item) {
        val motion = Vector3(0.0, 0.2, 0.0)
        val itemTag = NBTIO.putItemHelper(item)
        itemTag.name = "Item"

        val entities = this.level.getNearbyEntities(SimpleAxisAlignedBB(v.x - 1, v.y - 1, v.z - 1, v.x + 1, v.y + 1, v.z + 1))

        for (entity in entities) {
            if (entity is EntityItem) {

                if (!entity.closed && entity.isAlive && entity.item.count < 64 && entity.item.equals(item, true, false)) {
                    entity.item.count++
                    return
                }
            }
        }

        val itemEntity = SpecialItem(this.level.getChunk(v.getX().toInt() shr 4, v.getZ().toInt() shr 4, true), CompoundTag().putList(ListTag<Tag>("Pos").add(DoubleTag("", v.getX() + 0.5)).add(DoubleTag("", v.getY())).add(DoubleTag("", v.getZ() + 0.5))).putList(ListTag<Tag>("Motion").add(DoubleTag("", motion.x)).add(DoubleTag("", motion.y)).add(DoubleTag("", motion.z))).putList(ListTag<Tag>("Rotation").add(FloatTag("", Random().nextFloat() * 360.0f)).add(FloatTag("", 0.0f))).putShort("Health", 5).putCompound("Item", itemTag).putShort("PickupDelay", 0))

        if (item.id > 0 && item.getCount() > 0) {
            itemEntity.spawnToAll()
        }
    }

    fun messageAllPlayers(message: String) {
        messageAllPlayers(message, false)
    }

    fun messageAllPlayers(message: String, vararg args: String) {
        this.messageAllPlayers(message, null, null, false, *args)
    }

    fun messageAllPlayers(message: String, addPrefix: Boolean, vararg args: String) {
        this.messageAllPlayers(message, null, null, addPrefix, *args)
    }

    fun messageAllPlayers(message: String, player: Player, data: BedWarsData) {
        messageAllPlayers(message, player, data, false)
    }

    fun messageAllPlayers(message: String, player: Player?, data: BedWarsData?, addPrefix: Boolean, vararg args: String) {
        if (player != null) {
            val pData = getPlayerData(player) ?: return

            val color = "" + pData.team.chatColor
            val msg = TextFormat.GRAY.toString() + "[" + color + "All" + TextFormat.GRAY + "]   " + player.displayName + /*data.baseData.chatColor + ": " +*/ message.substring(1) //TODO: chatcolor

            for (p in ArrayList(playerData.values)) {
                p.player.sendMessage(msg)
            }

            for (p in spectators.values) {
                p.sendMessage(msg)
            }
            return
        }

        val translation = Language.translate(message, *args)

        playerData.values.forEach { it.player.sendMessage(if (addPrefix) BedWars.prefix else "" + translation) }
        spectators.values.forEach { it.sendMessage(if (addPrefix) BedWars.prefix else "" + translation) }

//        val translations = Language.getTranslations(message, *args)
//
//        for (pData in playerData.values) {
//            pData.player.sendMessage(if (addPrefix) BedWars.prefix else "" + translations[pData.baseData.language])
//        }
//
//        for (p in spectators.values) {
//            p.sendMessage(if (addPrefix) BedWars.prefix else "" + translations[MTCore.getInstance().getPlayerData(p).language])
//        }
    }

    @JvmOverloads
    fun selectMap(force: Boolean = false) {
        var map = ""
        var points = -1

        this.votingManager.stats.forEachIndexed { index, score ->
            if (points < score) {
                map = this.votingManager.currentTable[index]
                points = score
            }
        }

        if (this.plugin.server.isLevelLoaded(map)) {
            this.plugin.server.unloadLevel(this.plugin.server.getLevelByName(map))
        }

        WorldCopyTask(this.plugin, map, this.name, force)

        this.map = map

        this.mapConfig = plugin.maps[map]!!

        teams.forEach {
            it.mapConfig = this.mapConfig.teams[it.id]
        }

        messageAllPlayers("select_map", map)
    }

    private fun checkLobby() {
        if (this.playerData.size >= 12 && this.game == ArenaState.LOBBY) {
            this.starting = true
        } else if (this.playerData.size >= 16 && this.game == ArenaState.LOBBY && this.task.startTime > 10) {
            this.task.startTime = 10
        }
    }

    @EventHandler
    fun onItemHold(e: PlayerItemHeldEvent) {
        val p = e.player
        if (!inArena(p)) {
            return
        }

        if (this.game == ArenaState.LOBBY) {
            if (e.item.id == BlockID.STAINED_TERRACOTTA) {
                addToTeam(p, e.slot)
            }
        }
    }

    @EventHandler
    fun onArrowPickup(e: InventoryPickupArrowEvent) {
        e.setCancelled()
    }

    fun inArena(p: Player): Boolean {
        return this.playerData.containsKey(p.name.toLowerCase())
    }

    @EventHandler
    fun onBucketFill(e: PlayerBucketFillEvent) {
        val p = e.player
        if (!p.isOp || this.inArena(p)) {
            e.setCancelled()
        }
    }

    @EventHandler
    fun onBucketEmpty(e: PlayerBucketEmptyEvent) {
        val p = e.player
        if (!p.isOp || this.inArena(p)) {
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

    fun resetTeams() {
        this.initTeams()
    }

    fun isSpectator(p: Player): Boolean {
        return this.spectators.containsKey(p.name.toLowerCase())
    }

    @JvmOverloads
    fun setSpectator(p: Player, respawn: Boolean = false) {
        if (this.game == ArenaState.LOBBY || playerData.isEmpty() || isSpectator(p)) {
            return
        }

        var tpPos: Position = p.clone()

        unsetPlayer(p) //for sure

        if (!respawn) {
            val random = Random()
            tpPos = this.playerData[ArrayList(playerData.keys)[random.nextInt(playerData.size)]]!!.player
        }

        if (tpPos.y < 10) {
            tpPos.y = 10.0
        }

        this.spectators[p.name.toLowerCase()] = p

        p.isSneaking = false
        p.inventory.clearAll()

        p.inventory.setItem(5, ItemClock().setCustomName("" + TextFormat.ITALIC + TextFormat.AQUA + "Lobby"))
        p.inventory.sendContents(p)
        /**
         * Special method for spectator mode change
         */

        p.gamemode = 3

        p.adventureSettings.set(Type.ALLOW_FLIGHT, true)
        p.adventureSettings.set(Type.FLYING, true)
        p.adventureSettings.set(Type.WORLD_BUILDER, false)
        p.adventureSettings.set(Type.WORLD_IMMUTABLE, true)
        p.adventureSettings.set(Type.NO_CLIP, true)
        p.adventureSettings.update()
        p.despawnFromAll()

        val inventoryContentPacket = InventoryContentPacket()
        inventoryContentPacket.inventoryId = InventoryContentPacket.SPECIAL_CREATIVE
        p.dataPacket(inventoryContentPacket)

        p.inventory.sendContents(p)
        p.nameTag = p.name

        this.scoreabordManager.addPlayer(p)
        p.teleport(tpPos)
    }

    /*fun setSpectator(p: Player, respawn: Boolean) {
        if (this.game == 0) {
            return
        }

        val data = getPlayerData(p)

        if (data != null) {
            playerData.remove(p.name.toLowerCase())
        }


        if (mtcore.inLobby(p)) {
            this.mtcore.unsetLobby(p)
        }

        this.spectators.put(p.name.toLowerCase(), p)
        p.inventory.clearAll()
        p.teleport(Position(p.x, p.y + 10, p.z, this.level))
        p.isSneaking = false
        p.setGamemode(3)
        p.inventory.setItem(0, Item.get(Item.CLOCK))
        p.inventory.setHotbarSlotIndex(0, 0)
        p.inventory.sendContents(p)
    }*/

    fun unsetSpectator(p: Player) {
        this.spectators.remove(p.name.toLowerCase())
        this.scoreabordManager.removePlayer(p)

        p.adventureSettings.set(Type.ALLOW_FLIGHT, false)
        p.adventureSettings.set(Type.FLYING, false)
        p.adventureSettings.update()
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onProjectileHit(e: ProjectileHitEvent) {
        val ent = e.entity

        if (!::level.isInitialized || ent.getLevel().id != this.level.id) {
            return
        }

        if (ent is EntityArrow && ent.namedTag.contains("explode")) {
            val explosion = BedWarsExplosion(ent, 0.8, ent)

            explosion.explode(this, ent.namedTag.getInt("team"))
            ent.close()
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onChat(e: PlayerChatEvent) {
        val p = e.player

        if (spectators.containsKey(p.name.toLowerCase())) {
            val msg = TextFormat.GRAY.toString() + "[" + TextFormat.BLUE + "SPECTATE" + TextFormat.GRAY + "] " + TextFormat.RESET + TextFormat.WHITE + p.displayName + TextFormat.GRAY + " > " /*+ data2.chatColor*/ + e.message //TODO: chat color

            for (s in spectators.values) {
                s.sendMessage(msg)
            }

            e.setCancelled()
            return
        }

        val data = getPlayerData(p) ?: return

        if (e.message.startsWith("!") && e.message.length > 1) {
            this.messageAllPlayers(e.message, p, data)
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

        if (!inArena(p)) {
            return
        }

        if (inv2 is ShopWindow) {
            if (slot == 0) {
                val cost = inv2.cost
                val item = inv2.item

                if (!Items.containsItem(inv, cost)) {
                    p.sendMessage(Language.translate("low_shop", cost.customName))
                    return
                }

                if (!inv.canAddItem(item)) {
                    p.sendMessage(Language.translate("full_inventory"))
                    return
                }

                Items.removeItem(inv, cost)
                inv.addItem(item)

                p.sendMessage(BedWars.prefix + Language.translate("buy", if (item.hasCustomName()) item.customName else item.name))
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

    fun getPlayerData(p: Player) = playerData[p.name.toLowerCase()]

    @EventHandler
    fun onHungerChange(e: PlayerFoodLevelChangeEvent) {
        if (e.isCancelled) {
            return
        }

        val p = e.player

        if (inArena(p) && this.game == ArenaState.LOBBY) {
            e.setCancelled()
        }
    }

    //    @EventHandler
    fun onItemPickup(e: InventoryPickupItemEvent) {
        val item = e.item

        val p = e.inventory.holder as Player

//        val data = getPlayerData(p)

//        if (data != null && this.game >= 1 && item is SpecialItem) {
//            data.baseData.addExp(1)
//        }
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

            val data = getPlayerData(entity) ?: return

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

    fun getTeam(id: Int) = teams[id]

    private fun onEntityInteract(e: PlayerInteractEvent) {
        val p = e.player
        val b = e.block

        if (e.isCancelled) {
            return
        }

        if (e.action != Action.PHYSICAL || !::level.isInitialized || b.level.id != this.level.id || b.id != Block.STONE_PRESSURE_PLATE) {
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

    enum class ArenaState {
        LOBBY,
        GAME
    }

    companion object {

        const val MAX_PLAYERS = 16

        private val allowedBlocks = HashSet<Int>(listOf(Item.SANDSTONE, Block.STONE_PRESSURE_PLATE, 92, 30, 42, 54, 89, 121, 19, 92, Item.OBSIDIAN, Item.BRICKS, Item.ENDER_CHEST))
    }
}