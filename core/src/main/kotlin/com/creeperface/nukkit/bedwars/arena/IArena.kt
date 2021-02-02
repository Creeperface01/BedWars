package com.creeperface.nukkit.bedwars.arena

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.item.Item
import cn.nukkit.math.Vector3
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.arena.manager.ScoreboardManager
import com.creeperface.nukkit.bedwars.arena.manager.SignManager
import com.creeperface.nukkit.bedwars.obj.BedWarsData
import com.creeperface.nukkit.bedwars.utils.APIArena

interface IArena : APIArena {

    var plugin: BedWars
    val arenaPlayers: MutableMap<String, Player>

    val task: ArenaTask
    val popupTask: PopupTask

    val scoreboardManager: ScoreboardManager
    val signManager: SignManager

    var gamesCount: Int

    var canJoin: Boolean

    var handler: IArena

    val arenaLobby: Vector3

    override var closed: Boolean

    fun unsetAllPlayers()

    fun unsetPlayer(p: Player)

    fun messageAllPlayers(message: String, player: Player, data: BedWarsData? = null)

    fun tryJoinPlayer(p: Player, message: Boolean = true, action: IArena.() -> Unit = {}): Boolean

    fun initHandler()

    fun close() {
        this.closed = true
    }

    companion object {

        val allowedBlocks = setOf(
            Item.SANDSTONE,
            Block.STONE_PRESSURE_PLATE,
            92,
            30,
            42,
            54,
            89,
            121,
            19,
            92,
            Item.OBSIDIAN,
            Item.BRICKS,
            Item.ENDER_CHEST
        )

    }
}