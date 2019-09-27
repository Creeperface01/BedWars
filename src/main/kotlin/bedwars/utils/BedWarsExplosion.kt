package bedwars.utils

import bedwars.arena.Arena
import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.level.Level
import cn.nukkit.level.Position
import cn.nukkit.level.Sound
import cn.nukkit.level.particle.HugeExplodeParticle
import cn.nukkit.math.NukkitMath
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.math.Vector3
import cn.nukkit.network.protocol.ExplodePacket

/**
 * Created by CreeperFace on 14. 12. 2016.
 */
class BedWarsExplosion(private val source: Position, size: Double, private val what: Entity?) {

    private val level: Level = source.getLevel()
    private val size: Double = Math.max(size, 0.0)

    fun explode(arena: Arena, team: Int): Boolean {
        val source = Vector3(this.source.x, this.source.y, this.source.z).floor()

        val explosionSize = this.size * 2.0
        val minX = NukkitMath.floorDouble(this.source.x - explosionSize - 1.0).toDouble()
        val maxX = NukkitMath.ceilDouble(this.source.x + explosionSize + 1.0).toDouble()
        val minY = NukkitMath.floorDouble(this.source.y - explosionSize - 1.0).toDouble()
        val maxY = NukkitMath.ceilDouble(this.source.y + explosionSize + 1.0).toDouble()
        val minZ = NukkitMath.floorDouble(this.source.z - explosionSize - 1.0).toDouble()
        val maxZ = NukkitMath.ceilDouble(this.source.z + explosionSize + 1.0).toDouble()

        val explosionBB = SimpleAxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)

        val list = this.level.getNearbyEntities(explosionBB, this.what)
        for (entity in list) {

            if (entity is Player) {
                val data = arena.getPlayerData(entity)

                if (data?.team?.id == team)
                    continue
            }

            val distance = entity.distance(this.source) / explosionSize

            if (distance <= 1) {
                val motion = entity.subtract(this.source).normalize()
                val exposure = 1
                val impact = (1 - distance) * exposure
                val damage = ((impact * impact + impact) / 2 * 8.0 * explosionSize + 1).toInt()

                if (this.what != null) {
                    val ev = EntityDamageByEntityEvent(this.what, entity, DamageCause.ENTITY_EXPLOSION, damage.toFloat())
                    entity.attack(ev)
                } else {
                    val ev = EntityDamageEvent(entity, DamageCause.BLOCK_EXPLOSION, damage.toFloat())
                    entity.attack(ev)
                }

                entity.motion = motion.multiply(impact)
            }
        }

        val pk = ExplodePacket()
        pk.x = this.source.x.toFloat()
        pk.y = this.source.y.toFloat()
        pk.z = this.source.z.toFloat()
        pk.radius = this.size.toFloat()

        this.level.addChunkPacket(source.x.toInt() shr 4, source.z.toInt() shr 4, pk)

        this.level.addParticle(HugeExplodeParticle(source))
        this.level.addSound(source, Sound.RANDOM_EXPLODE)
        return true
    }
}
