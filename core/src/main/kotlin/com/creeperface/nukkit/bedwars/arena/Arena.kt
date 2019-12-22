package com.creeperface.nukkit.bedwars.arena

import cn.nukkit.AdventureSettings.Type
import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.block.Block
import cn.nukkit.block.BlockAir
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityBed
import cn.nukkit.entity.item.EntityItem
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.event.player.PlayerInteractEvent.Action
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
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.arena.Arena
import com.creeperface.nukkit.bedwars.api.arena.Arena.ArenaState
import com.creeperface.nukkit.bedwars.api.arena.PlayerData
import com.creeperface.nukkit.bedwars.api.arena.configuration.ArenaConfiguration
import com.creeperface.nukkit.bedwars.api.arena.configuration.IArenaConfiguration
import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.arena.manager.DeathManager
import com.creeperface.nukkit.bedwars.arena.manager.ScoreboardManager
import com.creeperface.nukkit.bedwars.arena.manager.SignManager
import com.creeperface.nukkit.bedwars.arena.manager.VotingManager
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityMine
import com.creeperface.nukkit.bedwars.blockentity.BlockEntityTeamSign
import com.creeperface.nukkit.bedwars.entity.SpecialItem
import com.creeperface.nukkit.bedwars.obj.BedWarsData
import com.creeperface.nukkit.bedwars.task.WorldCopyTask
import com.creeperface.nukkit.bedwars.utils.BedWarsExplosion
import com.creeperface.nukkit.bedwars.utils.Items
import com.creeperface.nukkit.bedwars.utils.blockEntity
import com.creeperface.nukkit.bedwars.utils.plus
import java.util.*
import kotlin.collections.ArrayList

class Arena(var plugin: BedWars, config: ArenaConfiguration) : Listener, IArenaConfiguration by config, Arena {

    val playerData = mutableMapOf<String, BedWarsData>()
    val gameSpectators = mutableMapOf<String, Player>()

    override val players: Map<String, PlayerData>
        get() = playerData.toMap()

    override val spectators: Map<String, Player>
        get() = gameSpectators.toMap()

    val teams = ArrayList<Team>(teamData.size)

    private val task = ArenaSchedule(this)
    private val popupTask = PopupTask(this)

    internal val votingManager: VotingManager
    internal val scoreboardManager = ScoreboardManager(this)
    internal val signManager = SignManager(this)
    internal val deathManager: DeathManager

    var map = "Voting"
    var winnerTeam = 0
    internal var canJoin = true
    var isLevelLoaded = false

    override var gameState = ArenaState.LOBBY
    override var starting = false
    override var ending = false

    override lateinit var level: Level
    override lateinit var mapConfig: MapConfiguration

    override val aliveTeams: List<Team>
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
        scoreboardManager.initVotes()
        initTeams()
        signManager.init()

        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun initTeams() {
        teams.clear()
        teams.addAll(teamData.mapIndexed { index, conf -> Team(this, index, conf) })
    }

    private fun enableScheduler() {
        this.plugin.server.scheduler.scheduleRepeatingTask(this.task, 20)
        this.plugin.server.scheduler.scheduleRepeatingTask(this.popupTask, 20)
    }

    override fun joinToArena(p: Player) {
        if (this.gameState == ArenaState.GAME) {
            p.sendMessage(BedWars.prefix + (Lang.JOIN_SPECTATOR.translate()))
            this.setSpectator(p)
            scoreboardManager.addPlayer(p)
            return
        }

        if (this.playerData.size >= this.maxPlayers && !p.hasPermission("bedwars.joinfullarena")) {
            p.sendMessage(BedWars.prefix + (Lang.GAME_FULL.translate()))
            return
        }

        if (!this.canJoin) {
            return
        }

        val data = plugin.players[p.id] ?: return
        data.arena = this

        val pl = BedWarsData(this, p, data)
        playerData[p.name.toLowerCase()] = pl

        p.nameTag = p.name
        p.sendMessage(BedWars.prefix + (Lang.JOIN.translate(this.name)))
        p.teleport(this.lobby)
        scoreboardManager.addPlayer(p)
        p.setSpawn(this.lobby)

        val inv = p.inventory
        inv.clearAll()

//        var i = 0
//        while (i++ < teams.size) {
//            val team = teams[i]
//
//            inv.setItem(i, Item.get(Item.STAINED_TERRACOTTA, team.color.woolData, 1).setCustomName("§r§7Join " + team.chatColor + team.name))
//        }

        inv.setItem(5, ItemClock().setCustomName("" + TextFormat.ITALIC + TextFormat.AQUA + "Lobby"))

        inv.sendContents(p)

        p.adventureSettings.set(Type.ALLOW_FLIGHT, false)
        p.gamemode = 3
        p.setGamemode(0)

        this.checkLobby()
        this.signManager.updateMainSign()
    }

    override fun leaveArena(p: Player) {
        if (isSpectator(p)) {
            unsetSpectator(p)
            return
        }

        val data = getPlayerData(p)

        data?.team?.let {
            val pTeam = data.team

            if (this.gameState == ArenaState.GAME) {
                pTeam.messagePlayers(Lang.PLAYER_LEAVE.translate(pTeam.chatColor + p.name))
                data.add(Stat.LOSSES)
            }

            if (p.isOnline) {
                p.sendMessage(BedWars.prefix + (Lang.LEAVE.translate()))
            }

            signManager.updateTeamSigns()
        }

        this.unsetPlayer(p)

        if (this.gameState == ArenaState.GAME) {
            this.checkAlive()
        }

        data?.globalData?.arena = null

        signManager.updateMainSign()
    }

    override fun startGame() {
        if (!isLevelLoaded) {
            return
        }

        this.task.startTime = this.startTime
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

                BlockEntityBed(this.level.getChunk(pos.chunkX, pos.chunkZ), nbt)
            }
        }

        this.level.time = 0
        this.level.stopTime()

        for (data in this.playerData.values) {
            if (!data.hasTeam()) {
                this.selectTeam(data)
            }
        }

        this.gameState = ArenaState.GAME
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

//            if (p.hasPermission("gameteam.vip")) { //TODO: bonus
//                val bronze = Items.BRONZE.clone()
//                bronze.setCount(16)
//                val iron = Items.IRON.clone()
//                iron.setCount(3)
//
//                p.inventory.addItem(bronze.clone())
//                p.inventory.addItem(iron.clone())
//                p.inventory.addItem(Items.GOLD.clone())
//            }
        }

        this.messageAllPlayers(Lang.START_GAME, false)

        scoreboardManager.initGame()
    }

    private fun selectTeam(data: BedWarsData) {
        var teamm: Team? = null
        val p = data.player

        for (team in teams) {
            if (!isTeamFull(team) || isTeamFree(team) || p.hasPermission("bedwars.joinfullteam")) {
                teamm = team
            }
        }

        teamm?.addPlayer(data)
    }

    internal fun checkAlive() {
        if (!this.ending) {
            val aliveTeams = this.aliveTeams

            if (aliveTeams.size == 1) {
                val team = aliveTeams[0]
                winnerTeam = team.id

                for (pl in team.players.values) {
                    val p = pl.player
                    p.sendMessage(BedWars.prefix + TextFormat.GOLD + "Obdrzel jsi 10 tokenu a 500 xp za vyhru!") //TODO: translate
                    pl.add(Stat.WINS)
                }

                messageAllPlayers(Lang.END_GAME, false, "" + team.chatColor, team.name)
                this.ending = true
            }

        }

        if (this.playerData.isEmpty()) {
            Server.getInstance().scheduler.scheduleDelayedTask(plugin, { stopGame() }, 1)
        }
    }

    override fun stopGame() {
        if (this.gameState == ArenaState.LOBBY) return

        this.unsetAllPlayers()
        this.task.gameTime = 0
        this.task.startTime = this.startTime
        this.task.drop = 0
        this.popupTask.ending = endingTime
        this.votingManager.players.clear()
        this.votingManager.createVoteTable()
        scoreboardManager.initVotes()
        this.ending = false
        this.winnerTeam = -1
        this.gameState = ArenaState.LOBBY

        this.level.unload()

        signManager.updateMainSign()
        scoreboardManager.reset()
    }

    private fun unsetAllPlayers() {
        ArrayList(this.playerData.values).forEach { unsetPlayer(it.player) }
        ArrayList(this.gameSpectators.values).forEach { this.unsetSpectator(it) }

        this.gameSpectators.clear()
        this.playerData.clear()
        this.initTeams()
    }

    override fun inArena(p: Player): Boolean {
        return this.playerData.containsKey(p.name.toLowerCase())
    }

    fun isJoinSign(b: Block): Int {
        return (b.blockEntity as? BlockEntityTeamSign)?.team ?: -1
    }

    internal fun onBedBreak(p: Player, bedteam: Team, b: Block): Boolean {
        val data = getPlayerData(p) ?: return false
        val pTeam = data.team

        if (pTeam.id == bedteam.id) {
            p.sendMessage(BedWars.prefix + (Lang.BREAK_OWN_BED.translate()))
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

        for (player in ArrayList(gameSpectators.values)) {
            pk.x = player.x.toInt().toFloat()
            pk.y = player.y.toInt() + player.eyeHeight
            pk.z = player.z.toInt().toFloat()
            player.dataPacket(pk)
        }

        val team = data.team
        val color = "" + team.chatColor
        val name = team.name

        messageAllPlayers(Lang.BED_BREAK, false, "" + bedteam.chatColor, color + p.name, color + name, bedteam.chatColor.toString() + bedteam.name)
        bedteam.onBedBreak()

        checkAlive()
        return true
    }

    internal fun isBed(b: Block): Team? {
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

    override fun getPlayerTeam(p: Player) = this.playerData[p.name.toLowerCase()]?.team

    fun isTeamFree(team: Team): Boolean {
        val minPlayers = this.teams.filter { it !== team }.minBy { it.players.size }?.players?.size ?: team.players.size

        return team.players.size - minPlayers < 2
    }

    override fun isTeamFree(team: com.creeperface.nukkit.bedwars.api.arena.Team) = isTeamFree(team as Team)

    fun addToTeam(p: Player, team: Int) {
        val pTeam = teams[team]
        val data = playerData[p.name.toLowerCase()]!!

        if ((isTeamFull(pTeam) || !isTeamFree(pTeam)) && !p.hasPermission("bedwars.joinfullteam")) {
            p.sendMessage(BedWars.prefix + (Lang.FULL_TEAM.translate()))
            return
        }

        val currentTeam = data.team

        if (currentTeam.id == pTeam.id) {
            p.sendMessage(BedWars.prefix + (Lang.ALREADY_IN_TEAM.translate(pTeam.chatColor.toString() + pTeam.name)))
            return
        }

        if (currentTeam.id != 0) {
            currentTeam.removePlayer(data)
        }

        pTeam.addPlayer(data)

        signManager.updateTeamSigns()

        p.sendMessage(Lang.TEAM_JOIN.translate(pTeam.chatColor.toString() + pTeam.name))
    }

    fun isTeamFull(team: Team): Boolean {
        return team.players.size >= 4
    }

    fun unsetPlayer(p: Player) {
        this.scoreboardManager.removePlayer(p)

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

    override fun messageAllPlayers(lang: Lang, vararg args: String) {
        messageAllPlayers(lang, false, *args)
    }

    override fun messageAllPlayers(lang: Lang, addPrefix: Boolean, vararg args: String) {
        val translation = lang.translate(*args)

        playerData.values.forEach { it.player.sendMessage(if (addPrefix) BedWars.prefix else "" + translation) }
        gameSpectators.values.forEach { it.sendMessage(if (addPrefix) BedWars.prefix else "" + translation) }
    }

    internal fun messageAllPlayers(lang: String, player: Player, data: BedWarsData? = null) {
        val pData = data ?: getPlayerData(player) ?: return

        val color = "" + pData.team.chatColor
        val msg = TextFormat.GRAY.toString() + "[" + color + "All" + TextFormat.GRAY + "]   " + player.displayName + /*data.baseData.chatColor + ": " +*/ lang.substring(1) //TODO: chatcolor

        for (p in ArrayList(playerData.values)) {
            p.player.sendMessage(msg)
        }

        for (p in gameSpectators.values) {
            p.sendMessage(msg)
        }
        return
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

        messageAllPlayers(Lang.SELECT_MAP, map)
    }

    private fun checkLobby() {
        if (this.playerData.size >= this.startPlayers && this.gameState == ArenaState.LOBBY) {
            this.starting = true
        } else if (this.playerData.size >= this.fastStartPlayers && this.gameState == ArenaState.LOBBY && this.task.startTime > this.fastStartTime) {
            this.task.startTime = this.fastStartTime
        }
    }

    override fun isSpectator(p: Player): Boolean {
        return this.gameSpectators.containsKey(p.name.toLowerCase())
    }

    override fun setSpectator(p: Player, respawn: Boolean) {
        if (this.gameState == ArenaState.LOBBY || playerData.isEmpty() || isSpectator(p)) {
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

        this.gameSpectators[p.name.toLowerCase()] = p

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

        this.scoreboardManager.addPlayer(p)
        p.teleport(tpPos)
    }

    fun unsetSpectator(p: Player) {
        this.gameSpectators.remove(p.name.toLowerCase())
        this.scoreboardManager.removePlayer(p)

        p.adventureSettings.set(Type.ALLOW_FLIGHT, false)
        p.adventureSettings.set(Type.FLYING, false)
        p.adventureSettings.update()
    }

    override fun getPlayerData(p: Player) = playerData[p.name.toLowerCase()]

    override fun getTeam(id: Int) = teams[id]

    internal fun onEntityInteract(e: PlayerInteractEvent) {
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

    companion object {

        internal val allowedBlocks = HashSet<Int>(listOf(Item.SANDSTONE, Block.STONE_PRESSURE_PLATE, 92, 30, 42, 54, 89, 121, 19, 92, Item.OBSIDIAN, Item.BRICKS, Item.ENDER_CHEST))
    }
}