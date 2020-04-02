package com.creeperface.nukkit.bedwars.arena

import cn.nukkit.entity.item.EntityFirework
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.scheduler.Task
import com.creeperface.nukkit.bedwars.api.arena.Arena.ArenaState
import com.creeperface.nukkit.bedwars.api.event.ArenaStopEvent
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.utils.FireworkUtils
import java.util.*

class PopupTask(var plugin: Arena) : Task() {

    var ending = 20
    private var fireworkIndex = 0

    override fun onRun(tick: Int) {
        /*if (this.plugin.game == 1 && !this.plugin.ending) {
            this.sendStatus();
        }*/
        if (this.plugin.ending && this.plugin.arenaState == ArenaState.GAME) {
            if (this.ending <= 0) {
                this.plugin.ending = false
                this.plugin.stopGame(ArenaStopEvent.Cause.ELIMINATION)
                this.ending = 20
                return
            }

            spawnFireworks()
            this.ending--
        }
        if (this.plugin.arenaState == ArenaState.LOBBY) {
            sendPlayerCount()
        }
    }

    private fun sendPlayerCount() {
        this.plugin.playerData.values.forEach {
            it.player.sendPopup(Lang.PLAYER_COUNT.translate(plugin.playerData.size.toString(), plugin.maxPlayers))
        }
    }

    private fun spawnFireworks() {
        val winnerTeam = plugin.winnerTeam ?: return

        val positions = ArrayList<Vector3>()

        for (team in plugin.teams) {
            positions.add(team.mapConfig.spawn)
        }

        if (fireworkIndex > 3) {
            fireworkIndex = 0
        }

        val firework = FireworkUtils.of(winnerTeam)[fireworkIndex++]
        val itemTag = NBTIO.putItemHelper(firework)

        for (pos in positions) {
            val nbt = CompoundTag()
                    .putList(ListTag<DoubleTag>("Pos")
                            .add(DoubleTag("", pos.x + 0.5))
                            .add(DoubleTag("", pos.y + 0.5))
                            .add(DoubleTag("", pos.z + 0.5)))
                    .putList(ListTag<DoubleTag>("Motion")
                            .add(DoubleTag("", 0.0))
                            .add(DoubleTag("", 0.0))
                            .add(DoubleTag("", 0.0)))
                    .putList(ListTag<FloatTag>("Rotation")
                            .add(FloatTag("", 0f))
                            .add(FloatTag("", 0f)))
                    .putCompound("FireworkItem", itemTag)

            val entity = EntityFirework(plugin.level.getChunk(pos.floorX shr 4, pos.floorZ shr 4), nbt)
            entity.motionX = NukkitRandom().nextRange(-5, 5).toDouble() / 10
            entity.motionZ = NukkitRandom().nextRange(-5, 5).toDouble() / 10

            entity.spawnToAll()
        }
    }
}