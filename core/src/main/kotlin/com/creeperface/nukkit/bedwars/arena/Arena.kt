package com.creeperface.nukkit.bedwars.arena

import cn.nukkit.Player
import cn.nukkit.math.Vector3
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.arena.Arena
import com.creeperface.nukkit.bedwars.api.arena.State
import com.creeperface.nukkit.bedwars.api.arena.configuration.ArenaConfiguration
import com.creeperface.nukkit.bedwars.api.arena.configuration.IArenaConfiguration
import com.creeperface.nukkit.bedwars.api.placeholder.ArenaScope
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.api.utils.invoke
import com.creeperface.nukkit.bedwars.arena.handler.ArenaTeamSelect
import com.creeperface.nukkit.bedwars.arena.handler.ArenaVoting
import com.creeperface.nukkit.bedwars.arena.manager.ScoreboardManager
import com.creeperface.nukkit.bedwars.arena.manager.SignManager
import com.creeperface.nukkit.bedwars.obj.BedWarsData
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Arena(override var plugin: BedWars, config: ArenaConfiguration) : IArenaConfiguration by config, IArena {

    override val arenaPlayers = mutableMapOf<String, Player>()

    override val players: Map<String, Player>
        get() = arenaPlayers.toMap()

    override val task = ArenaTask(this)
    override val popupTask = PopupTask(this)

    override val scoreboardManager = ScoreboardManager()
    override val signManager = SignManager(this)

    override var gamesCount = 0
    override var canJoin = true

    override var handler: IArena by HandlerDelegate()

    override val state: State<*>
        get() = handler.state

    override val context = ArenaScope.getContext(this)

    override val arenaLobby: Vector3
        get() = lobby ?: plugin.server.defaultLevel.spawnLocation

    override var closed = false

    init {
        initHandler()
        this.enableScheduler()

        signManager.init()
    }

    override fun initHandler() {
        scoreboardManager.reset()

        handler = if (voteConfig.enable) {
            ArenaVoting(this)
        } else {
            ArenaTeamSelect(this)
        }

        signManager.updateMainSign()
    }

    override fun getHandler(): Arena {
        if (this === this.handler) {
            return this
        }

        return this.handler
    }

    private fun enableScheduler() {
        this.plugin.server.scheduler.scheduleRepeatingTask(this.task, 20)
        this.plugin.server.scheduler.scheduleRepeatingTask(this.popupTask, 20)
    }

    override fun tryJoinPlayer(p: Player, message: Boolean, action: IArena.() -> Unit): Boolean {
        if (this.arenaPlayers.size >= this.maxPlayers && !p.hasPermission("bedwars.joinfullarena")) {
            message {
                p.sendMessage(Lang.GAME_FULL.translatePrefix())
            }
            return false
        }

        if (!this.canJoin) {
            return false
        }

        action()
        return true
    }

    override fun leaveArena(p: Player) {
        if (p.isOnline) {
            p.sendMessage(Lang.LEAVE.translatePrefix())
        }

        this.unsetPlayer(p)

        signManager.updateMainSign()
    }

    override fun unsetAllPlayers() {
        this.arenaPlayers.values.toList().forEach { unsetPlayer(it) }
        this.arenaPlayers.clear()
    }

    override fun inArena(p: Player): Boolean {
        return this.arenaPlayers.containsKey(p.name.toLowerCase())
    }

    override fun unsetPlayer(p: Player) {
        this.scoreboardManager.removePlayer(p)
        this.arenaPlayers.remove(p.name.toLowerCase())

        plugin.players[p.id].arena = null

        p.inventory.clearAll()
    }

    override fun joinToArena(p: Player) = this.handler.joinToArena(p)

    override fun messageAllPlayers(lang: Lang, vararg args: String) {
        messageAllPlayers(lang, false, *args)
    }

    override fun messageAllPlayers(lang: Lang, addPrefix: Boolean, vararg args: String) {
        val translation = lang.translate(*args)

        arenaPlayers.values.forEach { it.sendMessage(if (addPrefix) BedWars.chatPrefix else "" + translation) }
    }

    override fun messageAllPlayers(message: String, player: Player, data: BedWarsData?) {
        this.handler.messageAllPlayers(message, player, data)
    }

    override fun toString() = this.name

    class HandlerDelegate<T> : ReadWriteProperty<T, IArena> {

        var value: IArena? = null

        override fun getValue(thisRef: T, property: KProperty<*>): IArena {
            value.let {
                requireNotNull(it)
                return it
            }
        }

        override fun setValue(thisRef: T, property: KProperty<*>, value: IArena) {
            this.value?.close()

            this.value = value
        }
    }
}