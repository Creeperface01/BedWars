package com.creeperface.nukkit.bedwars.arena

import cn.nukkit.AdventureSettings.Type
import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.block.Block
import cn.nukkit.block.BlockAir
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityBed
import cn.nukkit.blockentity.BlockEntityChest
import cn.nukkit.blockentity.BlockEntitySign
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
import com.creeperface.nukkit.bedwars.blockEntity.BlockEntityMine
import com.creeperface.nukkit.bedwars.entity.SpecialItem
import com.creeperface.nukkit.bedwars.entity.TNTShip
import com.creeperface.nukkit.bedwars.entity.Villager
import com.creeperface.nukkit.bedwars.mySQL.Stat
import com.creeperface.nukkit.bedwars.obj.BedWarsData
import com.creeperface.nukkit.bedwars.obj.Language
import com.creeperface.nukkit.bedwars.obj.Team
import com.creeperface.nukkit.bedwars.shop.ItemWindow
import com.creeperface.nukkit.bedwars.shop.ShopWindow
import com.creeperface.nukkit.bedwars.shop.Window
import com.creeperface.nukkit.bedwars.task.WorldCopyTask
import com.creeperface.nukkit.bedwars.utils.*
import java.util.*

class Arena(var id: String, var plugin: BedWars) : Listener {

    val data = mutableMapOf<String, Vector3>()
    private val mainData: Map<String, Vector3> = this.plugin.arenas[this.id]!!.toMap()
    lateinit var level: Level

    var playerData = HashMap<String, BedWarsData>()
    var spectators = HashMap<String, Player>()

    var teams = arrayOfNulls<Team>(5)
    var game = 0
    var starting = false
    var ending = false
    val task = ArenaSchedule(this)
    val popupTask = PopupTask(this)
    var votingManager: VotingManager
    var deathManager: DeathManager
    var map = "Voting"
    var winnerTeam: Int = 0
    private var canJoin = true

    var bossBar: BossBar
    var barUtil: BossBarUtil

    val isMultiPlatform: Boolean

    var isLevelLoaded = false

    val gameStatus: String
        get() {
            val t1 = teams[1]!!
            val t2 = teams[2]!!
            val t3 = teams[3]!!
            val t4 = teams[4]!!

            return "                                          §8Mapa: §6" + this.map + "\n" + t1.status + t2.status + t3.status + t4.status + "\n" + "\n"
        }

    val aliveTeams: ArrayList<Team>
        get() {
            val teams = ArrayList<Team>()

            for (team in this.teams) {
                if (team == null) {
                    continue
                }

                if (team.hasBed() || team.players.isNotEmpty()) {
                    teams.add(team)
                }
            }

            return teams
        }

    init {
        this.isMultiPlatform = !mainData.containsKey("multiplatform")

        this.bossBar = BossBar(this.plugin)
        this.barUtil = BossBarUtil(this)
        this.enableScheduler()
        this.votingManager = VotingManager(this)
        this.deathManager = DeathManager(this)
        this.votingManager.createVoteTable()
        this.writeTeams()
        updateMainSign()
        updateTeamSigns()
        this.barUtil.updateBar(0)

        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun enableScheduler() {
        this.plugin.server.scheduler.scheduleRepeatingTask(this.task, 20)
        this.plugin.server.scheduler.scheduleRepeatingTask(this.popupTask, 20)
    }

    private fun writeTeams() {
        this.teams[1] = Team(this, 1, "blue", TextFormat.BLUE, Color.toDecimal(Color.BLUE))
        this.teams[2] = Team(this, 2, "red", TextFormat.RED, Color.toDecimal(Color.RED))
        this.teams[3] = Team(this, 3, "yellow", TextFormat.YELLOW, Color.toDecimal(Color.YELLOW))
        this.teams[4] = Team(this, 4, "green", TextFormat.GREEN, Color.toDecimal(Color.GREEN))
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

        if (e.action == Action.RIGHT_CLICK_BLOCK && b == mainData["sign"]) {
            e.setCancelled()

            if (plugin.getPlayerArena(p) != null)
                return

            if (!this.isMultiPlatform && p.loginChainData.deviceOS == 7 && !p.hasPermission("gameteam.helper") && !p.hasPermission("gameteam.mcpe")) {
                p.sendMessage(Lang.translate("pe_only", p))
                return
            }

            this.joinToArena(p)
            return
        }

        if (isSpectator(p)) {
            if (e.action == Action.RIGHT_CLICK_AIR && e.item.id == Item.CLOCK) {
                this.leaveArena(p)
                return
            }
        }

//        if (data1.isInLobby) {
//            e.setCancelled()
//            return
//        }

//        if (e.isCancelled) {
//            return
//        }

        val data = getPlayerData(p) ?: return

        if (this.game == 0 && e.action == Action.RIGHT_CLICK_AIR && e.item.id == Item.CLOCK) {
            this.leaveArena(p)
            return
        }

        val team = this.isJoinSign(b)

        if (team != 0) {
            this.addToTeam(p, team)
            return
        }

        if (e.action == Action.RIGHT_CLICK_BLOCK && data.team != null && b.id == Block.ENDER_CHEST) {
            e.setCancelled()
            p.addWindow(data.team!!.enderChest)
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

            val ship = TNTShip(b.getLevel().getChunk(b.floorX shr 4, b.floorZ shr 4), nbt, this, data.team!!)
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
        if (this.game >= 1) {
            p.sendMessage(BedWars.prefix + Language.translate("join_spectator"))
            this.setSpectator(p)
            bossBar.addPlayer(p)
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
        p.sendMessage(BedWars.prefix + Language.translate("join", this.id))
        p.teleport(this.mainData["lobby"])
        bossBar.addPlayer(p)
        p.setSpawn(this.mainData["lobby"])

        val inv = p.inventory
        inv.clearAll()
        inv.setItem(0, Item.get(159, 11, 1).setCustomName("§r§7Pripojit do §9Blue"))
        inv.setItem(1, Item.get(159, 14, 1).setCustomName("§r§7Pripojit do §4Red"))
        inv.setItem(2, Item.get(159, 4, 1).setCustomName("§r§7Pripojit do §eYellow"))
        inv.setItem(3, Item.get(159, 5, 1).setCustomName("§r§7Pripojit do §aGreen"))


        inv.setItem(5, ItemClock().setCustomName("" + TextFormat.ITALIC + TextFormat.AQUA + "Lobby"))

        inv.sendContents(p)

        p.adventureSettings.set(Type.ALLOW_FLIGHT, false)
        p.gamemode = 3
        p.setGamemode(0)

        this.checkLobby()
        this.updateMainSign()
    }

    fun leaveArena(p: Player) {
        if (isSpectator(p)) {
            unsetSpectator(p)
            return
        }

        val data = getPlayerData(p)

        data?.team?.let {
            val pTeam = data.team

            if (this.game >= 1) {
                pTeam!!.messagePlayers(Language.translate("player_leave", pTeam.color.toString() + p.name))
                data.add(Stat.LOSSES)
            }

            if (p.isOnline) {
                p.sendMessage(BedWars.prefix + Language.translate("leave"))
            }

            updateTeamSigns()
        }

        this.unsetPlayer(p)

        if (this.game >= 1) {
            this.checkAlive()
        }

        data?.globalData?.arena = null

        updateMainSign()
    }

    fun startGame() {
        if (!isLevelLoaded) {
            return
        }

        this.task.startTime = 50
        this.starting = false
        isLevelLoaded = false

        this.plugin.server.loadLevel(this.map + "_" + id)
        this.level = this.plugin.server.getLevelByName(this.map + "_" + id)
        this.level.isRaining = false
        this.level.isThundering = false

        for (team in teams) {
            if (team == null) {
                continue
            }

            val positions = arrayOf(this.data[team.id.toString() + "bed"]!!, this.data[team.id.toString() + "bed2"]!!)

            for (pos in positions) {
                val nbt = BlockEntity.getDefaultCompound(pos, BlockEntity.BED)
                nbt.putByte("color", team.dyeColor.woolData)

                BlockEntityBed(this.level.getChunk(pos.floorX shr 4, pos.floorZ shr 4), nbt)
            }
        }

        this.level.time = 0
        this.level.stopTime()

        for (data in ArrayList(this.playerData.values)) {
            if (data.team == null) {
                this.selectTeam(data)
            }
        }

        this.game = 1
        this.updateMainSign()

        for (data in playerData.values) {
            val p = data.player

            val d = this.data[data.team!!.id.toString() + "spawn"]!!

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
        barUtil.updateBar(0)
        barUtil.updateTeamStats()
        this.bossBar.maxHealth = 100
        this.bossBar.health = 100
        this.bossBar.update()
    }

    fun selectTeam(data: BedWarsData) {
        var teamm: Team? = null
        val p = data.player

        for (team in teams) {
            if (team == null) {
                continue
            }

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

    fun checkAlive() {
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

                messageAllPlayers("end_game", false, "" + team.color, team.name)
                this.ending = true
            }

        }

        if (this.playerData.size <= 0) {
            Server.getInstance().scheduler.scheduleDelayedTask(plugin, { stopGame() }, 1)
        }
    }

    fun stopGame() {
//        for (data in this.playerData.values) {
//            data.baseData.addExp(200) //played
//        }

        this.unsetAllPlayers()
        this.task.gameTime = 0
        this.task.startTime = 50
        this.task.drop = 0
        this.task.sign = 0
        this.popupTask.ending = 20
        this.votingManager.players.clear()
        this.votingManager.currentTable = arrayOfNulls(4)
        this.votingManager.stats.clear()
        this.votingManager.createVoteTable()
        this.ending = false
        this.winnerTeam = 0
        this.game = 0

        this.level.unload()

        updateMainSign()
        barUtil.updateBar(0)
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

        if (!p.isOp && this.game == 0) {
            e.setCancelled()
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onHit(e: EntityDamageEvent) {
        val victim = e.entity

        if (!::level.isInitialized || victim.getLevel().id != this.level.id) {
            if (victim is Player) {

                if (this.game == 0 && e.cause == DamageCause.VOID && inArena(victim)) {
                    victim.teleport(mainData["lobby"])
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

            if (this.game == 0) {
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

                    if (this.game < 1 || data!!.team!!.id == kData.team!!.id) {
                        e.setCancelled()
                        return
                    }

                    if (!kill) {
                        data.lastHit = System.currentTimeMillis()
                        data.killer = killer.name
                        data.killerColor = "" + kData.team!!.color
                    }
                }

                if (e !is EntityDamageByChildEntityEvent && victim.networkId == Villager.NETWORK_ID && killer.getGamemode() == 0) {
                    kData = playerData[killer.name.toLowerCase()]!!
                    val inv = kData.team!!.shop

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
                p.teleport(this.data[data.team!!.id.toString() + "spawn"])
            }

            bossBar.update(p, true)
        }
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

        if (this.game == 0) {
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
                messageAllPlayers("legend_found", data.team!!.color.toString() + p.name, randomItem.customName)
            }

            MainLogger.getLogger().info("LB item: ${randomItem.name}")

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

        if (this.game == 0) {
            e.setCancelled()
            return
        }

        if (b.id == Block.STONE_PRESSURE_PLATE && data.team != null) {
            val nbt = CompoundTag()
                    .putList(ListTag("Items"))
                    .putString("id", BlockEntity.FURNACE)
                    .putInt("x", b.floorX)
                    .putInt("y", b.floorY)
                    .putInt("z", b.floorZ)
                    .putInt("team", data.team!!.id)

            BlockEntityMine(b.level.getChunk(b.floorX shr 4, b.floorZ shr 4), nbt)
        }

//        data.baseData.addExp(1)
    }

    private fun onBedBreak(p: Player, bedteam: Team, b: Block): Boolean {
        val data = getPlayerData(p)
        val pTeam = data!!.team

        if (pTeam!!.id == bedteam.id) {
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

        val team = this.getPlayerTeam(p)!!.id
        val color = "" + this.teams[team]!!.color
        val name = this.teams[team]!!.name

        messageAllPlayers("bed_break", false, "" + bedteam.color, color + p.name, color + name, bedteam.color.toString() + bedteam.name)
        bedteam.onBedBreak()

        checkAlive()
        return true
    }

    fun isJoinSign(b: Block): Int {
        return if (b == mainData["1sign"]) {
            1
        } else if (b == mainData["2sign"]) {
            2
        } else if (b == mainData["3sign"]) {
            3
        } else if (b == mainData["4sign"]) {
            4
        } else {
            0
        }
    }

    fun isBed(b: Block): Team? {
        if (b.id != Item.BED_BLOCK) {
            return null
        }

        val b1 = this.data["1bed"]
        val b12 = this.data["1bed2"]
        val b2 = this.data["2bed"]
        val b22 = this.data["2bed2"]
        val b3 = this.data["3bed"]
        val b32 = this.data["3bed2"]
        val b4 = this.data["4bed"]
        val b42 = this.data["4bed2"]

        if (b == b1 || b == b12) {
            return teams[1]
        } else if (b == b2 || b == b22) {
            return teams[2]
        } else if (b == b3 || b == b32) {
            return teams[3]
        } else if (b == b4 || b == b42) {
            return teams[4]
        }

        return null
    }

    fun getPlayerTeam(p: Player): Team? {
        return if (this.playerData.containsKey(p.name.toLowerCase())) {
            this.playerData[p.name.toLowerCase()]!!.team
        } else null
    }

    fun getPlayerColor(p: Player): Int {
        return if (this.playerData.containsKey(p.name)) {
            this.playerData[p.name]!!.team!!.decimal
        } else 0
    }

    fun isTeamFree(team: Team): Boolean {
        val teams = ArrayList<Int>()

        for (i in 1..4) {
            if (i == team.id) {
                continue
            }

            teams.add(this.teams[i]!!.players.size)
        }

        val minPlayers = Math.min(teams[2], Math.min(teams[0], teams[1]))

        return team.players.size - minPlayers < 2
    }

    fun addToTeam(p: Player, team: Int) {
        val pTeam = teams[team]!!
        val data = playerData[p.name.toLowerCase()]!!

        if ((isTeamFull(pTeam) || !isTeamFree(pTeam)) && !p.hasPermission("gameteam.vip")) {
            p.sendMessage(BedWars.prefix + Language.translate("full_team"))
            return
        }

        val currentTeam = data.team

        if (currentTeam != null) {
            if (currentTeam.id == pTeam.id) {
                p.sendMessage(BedWars.prefix + Language.translate("already_in_team", pTeam.color.toString() + pTeam.name))
                return
            }

            if (currentTeam.id != 0) {
                currentTeam.removePlayer(data)
            }
        }

        pTeam.addPlayer(data)

        updateTeamSigns()

        p.sendMessage(Language.translate("team_join", pTeam.color.toString() + pTeam.name))
    }

    fun isTeamFull(team: Team): Boolean {
        return team.players.size >= 4
    }

    fun unsetPlayer(p: Player) {
        this.bossBar.removePlayer(p)

        val data = playerData.remove(p.name.toLowerCase())

        if (data != null) {
            data.globalData.arena = null

            if (data.team != null)
                data.team!!.removePlayer(data)
        }
    }

    fun dropBronze() {
        this.dropItem(this.data["1bronze"]!!, Items.BRONZE)
        this.dropItem(this.data["2bronze"]!!, Items.BRONZE)
        this.dropItem(this.data["3bronze"]!!, Items.BRONZE)
        this.dropItem(this.data["4bronze"]!!, Items.BRONZE)
    }

    fun dropIron() {
        this.dropItem(this.data["1iron"]!!, Items.IRON)
        this.dropItem(this.data["2iron"]!!, Items.IRON)
        this.dropItem(this.data["3iron"]!!, Items.IRON)
        this.dropItem(this.data["4iron"]!!, Items.IRON)
    }

    fun dropGold() {
        this.dropItem(this.data["1gold"]!!, Items.GOLD)
        this.dropItem(this.data["2gold"]!!, Items.GOLD)
        this.dropItem(this.data["3gold"]!!, Items.GOLD)
        this.dropItem(this.data["4gold"]!!, Items.GOLD)
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
            var msg: String

            if (data!!.team == null) {
                msg = TextFormat.GRAY.toString() + "[" + TextFormat.DARK_PURPLE + "Lobby" + TextFormat.GRAY + "] " + player.displayName + TextFormat.DARK_AQUA + " > " + /*data.baseData.chatColor +*/ message //TODO: chatcolor
            } else {
                val color = "" + getPlayerTeam(player)!!.color
                msg = TextFormat.GRAY.toString() + "[" + color + "All" + TextFormat.GRAY + "]   " + player.displayName + /*data.baseData.chatColor + ": " +*/ message.substring(1) //TODO: chatcolor
            }

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

        for ((key, value) in this.votingManager.stats) {
            if (points < value) {
                map = key
                points = value
            }
        }

        if (this.plugin.server.isLevelLoaded(map)) {
            this.plugin.server.unloadLevel(this.plugin.server.getLevelByName(map))
        }

        WorldCopyTask(this.plugin, map, this.id, force)

        this.map = map

        this.data.clear()
        this.data.putAll(this.plugin.maps[map]!!)

        teams.forEach {
            it ?: return@forEach

            it.spawn = this.data["${it.id}spawn"]!!
        }

        messageAllPlayers("select_map", map)
    }

    fun checkLobby() {
        if (this.playerData.size >= 12 && this.game == 0) {
            this.starting = true
        } else if (this.playerData.size >= 16 && this.game == 0 && this.task.startTime > 10) {
            this.task.startTime = 10
        }
    }

    @EventHandler
    fun onItemHold(e: PlayerItemHeldEvent) {
        val p = e.player
        if (!inArena(p)) {
            return
        }

        if (this.game <= 0) {
            if (e.item.id == 159) {
                when (e.item.damage) {
                    11 -> {
                        this.addToTeam(p, 1)
                        e.setCancelled()
                    }
                    14 -> {
                        this.addToTeam(p, 2)
                        e.setCancelled()
                    }
                    4 -> {
                        this.addToTeam(p, 3)
                        e.setCancelled()
                    }
                    5 -> {
                        this.addToTeam(p, 4)
                        e.setCancelled()
                    }
                }
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
        this.writeTeams()
    }

    fun isSpectator(p: Player): Boolean {
        return this.spectators.containsKey(p.name.toLowerCase())
    }

    @JvmOverloads
    fun setSpectator(p: Player, respawn: Boolean = false) {
        if (this.game == 0 || playerData.isEmpty() || isSpectator(p)) {
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

        this.bossBar.addPlayer(p)
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
        this.bossBar.removePlayer(p)

        p.adventureSettings.set(Type.ALLOW_FLIGHT, false)
        p.adventureSettings.set(Type.FLYING, false)
        p.adventureSettings.update()
    }

    fun isChest(b: Block): Boolean {
        return (b == this.data["1bronze"] || b == this.data["2bronze"] || b == this.data["3bronze"] || b == this.data["4bronze"])
    }

    fun isEnderChest(b: Block): Boolean {
        return (b == this.data["1chest"] || b == this.data["2chest"] || b == this.data["3chest"] || b == this.data["4chest"])
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

    fun getTeamEnderChest(team: Int): BlockEntityChest {
        return this.level.getBlockEntity(this.data["${team}chest"]) as BlockEntityChest
    }

    @EventHandler(ignoreCancelled = true)
    fun onChat(e: PlayerChatEvent) {
        val p = e.player
        val data = getPlayerData(p)
        val spectator = spectators[p.name.toLowerCase()]

        if ((data == null && spectator == null)) {
            return
        }

        e.setCancelled()

        if (spectator != null) {
            val msg = TextFormat.GRAY.toString() + "[" + TextFormat.BLUE + "SPECTATE" + TextFormat.GRAY + "] " + TextFormat.RESET + TextFormat.WHITE + p.displayName + TextFormat.GRAY + " > " /*+ data2.chatColor*/ + e.message //TODO: chat color

            for (s in spectators.values) {
                s.sendMessage(msg)
            }
        } else if (e.message.startsWith("!") && e.message.length > 1) {
            this.messageAllPlayers(e.message, p, data!!)
        } else if (data!!.team != null) {
            data.team!!.messagePlayers(e.message, data)
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

    private fun updateMainSign() {
        val pos = this.mainData["sign"]

        var tile = this.plugin.level.getBlockEntity(pos)
        if ((tile !is BlockEntitySign)) {
            val nbt = CompoundTag()
                    .putString("id", BlockEntity.SIGN)
                    .putInt("x", pos!!.x.toInt())
                    .putInt("y", pos.y.toInt())
                    .putInt("z", pos.z.toInt())
                    .putString("Text1", "")
                    .putString("Text2", "")
                    .putString("Text3", "")
                    .putString("Text4", "")

            tile = BlockEntitySign(this.plugin.level.getChunk(pos.x.toInt() shr 4, pos.z.toInt() shr 4), nbt)
        }


        val mapname = this.map
        val map: String

        map = when {
            this.game <= 0 -> if (isMultiPlatform) "---" else "" + TextFormat.BOLD + TextFormat.LIGHT_PURPLE + "PE ONLY"
            else -> mapname
        }
        var game = "§aLobby"
        if (this.game == 1) {
            game = "§cIngame"
        }
        if (this.game != 1 && !this.canJoin) {
            game = "§c§lRESTART"
        }
        tile.setText("§4■" + this.id + "■", "§0" + this.playerData.size + "/16", game, "§l§0$map")
    }

    fun updateTeamSigns() {
        val blue = this.plugin.level.getBlockEntity(this.mainData["1sign"]) as? BlockEntitySign ?: return
        val red = this.plugin.level.getBlockEntity(this.mainData["2sign"]) as? BlockEntitySign ?: return
        val yellow = this.plugin.level.getBlockEntity(this.mainData["3sign"]) as? BlockEntitySign ?: return
        val green = this.plugin.level.getBlockEntity(this.mainData["4sign"]) as? BlockEntitySign ?: return

        blue.setText("", "§l§9[BLUE]", "§7" + this.teams[1]!!.players.size + " players", "")
        red.setText("", "§l§c[RED]", "§7" + this.teams[2]!!.players.size + " players", "")
        yellow.setText("", "§l§e[YELLOW]", "§7" + this.teams[3]!!.players.size + " players", "")
        green.setText("", "§l§a[GREEN]", "§7" + this.teams[4]!!.players.size + " players", "")
    }

    fun getPlayerData(p: Player): BedWarsData? {
        return playerData[p.name.toLowerCase()]
    }

    @EventHandler
    fun onHungerChange(e: PlayerFoodLevelChangeEvent) {
        if (e.isCancelled) {
            return
        }

        val p = e.player

        if (inArena(p) && this.game <= 0) {
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
        val entity = e.getEntity()
        val bow = e.getBow()

        if (entity is Player) {
            val p = entity

            if (p.gamemode > 1) {
                e.setCancelled()
            }

            val data = getPlayerData(p) ?: return

            if (bow.customName == "Explosive Bow") {
                val entityBow = e.projectile

                entityBow.namedTag.putBoolean("explode", true)
                entityBow.namedTag.putInt("team", data.team!!.id)
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

    fun getTeam(id: Int): Team {
        return teams[id]!!
    }

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

        if (blockEntity.getTeam() == data.team!!.id) {
            e.setCancelled()
            return
        }

        e.setCancelled()
        BedWarsExplosion(b.add(0.5, 0.5, 0.5), 0.8, null).explode(this, blockEntity.getTeam())
        b.level.setBlock(b, BlockAir(), true, false)
    }

    companion object {

        val MAX_PLAYERS = 16

        private val allowedBlocks = HashSet<Int>(Arrays.asList<Int>(Item.SANDSTONE, Block.STONE_PRESSURE_PLATE, 92, 30, 42, 54, 89, 121, 19, 92, Item.OBSIDIAN, Item.BRICKS, Item.ENDER_CHEST))
    }
}