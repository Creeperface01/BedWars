package bedWars.arena

import bedWars.`object`.Language
import bedWars.utils.FireworkUtils
import cn.nukkit.entity.item.EntityFirework
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.scheduler.Task
import java.util.*

class PopupTask(var plugin: Arena) : Task() {

    var ending = 20
    var fireworkIndex = 0

    override fun onRun(tick: Int) {
        /*if (this.plugin.game == 1 && !this.plugin.ending) {
            this.sendStatus();
        }*/
        if (this.plugin.ending && this.plugin.game == 1) {
            if (this.ending <= 0) {
                this.plugin.ending = false
                this.plugin.stopGame()
                this.ending = 20
                return
            }

            this.ending--
            this.sendEnding()
        }
        if (this.plugin.game == 0) {
            sendPlayerCount()
        }
    }

    private fun sendPlayerCount() {
        this.plugin.playerData.values.forEach {
            it.player.sendPopup(Language.translate("player_count", plugin.playerData.size.toString(), "16"))
        }
    }

    fun sendVotes() {
        val vm = this.plugin.votingManager
        val votes = arrayOf(vm.currentTable[0], vm.currentTable[1], vm.currentTable[2])

        val tip = ("                                                   §8Voting §f| §6/vote <map>"
                + "\n                                                 §b[1] §8" + votes[0] + " §c» §a" + vm.stats[votes[0]] + " hlasu"
                + "\n                                                 §b[2] §8" + votes[1] + " §c» §a" + vm.stats[votes[1]] + " hlasu"
                + "\n                                                 §b[3] §8" + votes[2] + " §c» §a" + vm.stats[votes[2]] + " hlasu\n\n\n\n")

        for (data in ArrayList(this.plugin.playerData.values)) {
            data.player.sendPopup(tip)
        }                        //    |
    }

    fun sendStatus() {
        val status = this.plugin.gameStatus

        for (data in ArrayList(this.plugin.playerData.values)) {
            data.player.sendPopup(status)
        }

        for (p in ArrayList(plugin.spectators.values)) {
            p.sendPopup(status)
        }
    }

    fun sendEnding() {
//        for (data in ArrayList(this.plugin.playerData.values)) {
//            val p = data.player
//            this.plugin.level.addSound(p.add(0.toDouble(), p.eyeHeight.toDouble()), Sound.RANDOM_FIZZ, 1f, 1f)
//
//            val random = randomVector(p)
//
//            val nbt = CompoundTag()
//                    .putList(ListTag<DoubleTag>("Pos")
//                            .add(DoubleTag("", random.x))
//                            .add(DoubleTag("", random.y))
//                            .add(DoubleTag("", random.z)))
//                    .putList(ListTag<DoubleTag>("Motion")
//                            .add(DoubleTag("", 0.0))
//                            .add(DoubleTag("", 0.0))
//                            .add(DoubleTag("", 0.0)))
//
//                    .putList(ListTag<FloatTag>("Rotation")
//                            .add(FloatTag("", 0.toFloat()))
//                            .add(FloatTag("", 0.toFloat())))
//
//            WinParticle(plugin.level.getChunk(random.x.toInt() shr 4, random.z.toInt() shr 4), nbt)
//        }
//
//        for (p in ArrayList(plugin.spectators.values)) {
//            this.plugin.level.addSound(p.add(0.toDouble(), p.eyeHeight.toDouble()), Sound.RANDOM_FIZZ, 1f, 1f)
//        }
        spawnFireworks()

        plugin.barUtil.updateBar(ending)
    }

    private fun randomVector(center: Vector3): Vector3 {
        val rnd = Random()

        val x = (rnd.nextInt(8) - 4).toDouble()
        val z = (rnd.nextInt(8) - 4).toDouble()
        val y = rnd.nextInt(4 - 2).toDouble()

        return Vector3(center.x + x, center.y + y, center.z + z)
    }

    private fun spawnFireworks() {
        val positions = ArrayList<Vector3>()

        for (team in plugin.teams) {
            team ?: continue

            positions.add(team.spawn)
        }

        if (fireworkIndex > 3) {
            fireworkIndex = 0
        }

        val firework = FireworkUtils.of(plugin.getTeam(plugin.winnerTeam)).get(fireworkIndex++)
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