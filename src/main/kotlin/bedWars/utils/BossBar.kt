package bedWars.utils

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.entity.Attribute
import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.EntityMetadata
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3
import cn.nukkit.network.protocol.*
import cn.nukkit.plugin.Plugin
import java.util.*

/**
 * @author CreeperFace
 */
class BossBar(private val plugin: Plugin) {

    private val players = HashMap<String, Player>()

    val id: Long = 2197383491L

    var health = 1
        set(health) {
            field = Math.max(health, 1)
        }

    var maxHealth = 600
        set(health) {
            field = Math.max(health, 1)
        }

    private val metadata: EntityMetadata = EntityMetadata().putString(Entity.DATA_NAMETAG, "").putLong(0, 196640L).putLong(38, -1L).putFloat(54, 0.0f).putFloat(55, 0.0f).putFloat(39, 0.0f)

    private val permanentPacket = BossEventPacket()
    private val attributesPacket = UpdateAttributesPacket()
    private val random = NukkitRandom()

    init {
        this.permanentPacket.bossEid = this.id
        this.permanentPacket.type = 0

        val permanentPacketUpdate = BossEventPacket()
        permanentPacketUpdate.type = 1
        permanentPacketUpdate.color = 0x4286f4
        permanentPacketUpdate.overlay = 0x4286f4

        this.attributesPacket.entityId = this.id
        this.attributesPacket.entries = arrayOf(Attribute.getAttribute(4).setMaxValue(this.maxHealth.toFloat()).setValue(this.health.toFloat()))
        this.attributesPacket.encode()
        this.attributesPacket.isEncoded = true

        plugin.server.scheduler.scheduleDelayedRepeatingTask(plugin, { this@BossBar.update() }, 10, 10)
    }

    fun addPlayer(p: Player) {
        this.players[p.name.toLowerCase()] = p
        val pos = p.add(this.getDirectionVector(p).normalize().multiply(-15.0))
        val pk = AddEntityPacket()

        pk.type = WITHER_ID
        pk.entityRuntimeId = this.id
        pk.entityUniqueId = this.id
        pk.x = pos.x.toFloat()
        pk.y = (pos.y - 7).toFloat()
        pk.z = pos.z.toFloat()
        pk.speedX = 0f
        pk.speedY = 0f
        pk.speedZ = 0f
        pk.yaw = 0f
        pk.pitch = 0f
        pk.metadata = this.metadata

        val pk1 = UpdateAttributesPacket()
        pk1.entityId = this.id
        pk1.entries = arrayOf(Attribute.getAttribute(Attribute.MAX_HEALTH).setMaxValue(this.maxHealth.toFloat()).setValue(this.health.toFloat()))
        p.dataPacket(pk)
        p.dataPacket(pk1)
        p.dataPacket(this.attributesPacket)
        p.dataPacket(this.permanentPacket)
    }

    fun removePlayer(p: Player) {
        this.removePlayer(p.name)
        if (p.isOnline) {
            val pk = RemoveEntityPacket()
            pk.eid = this.id
            p.dataPacket(pk)
            val pk2 = BossEventPacket()
            pk2.bossEid = this.id
            pk2.type = 2
            p.dataPacket(pk2)
        }

    }

    fun removePlayer(name: String) {
        this.players.remove(name.toLowerCase())
    }

    fun update() {
        this.players.values.forEach({ this.update(it) })
    }

    fun update(p: Player) {
        this.update(p, false)
    }

    fun update(p: Player, respawn: Boolean) {
        val pos = p.add(this.getDirectionVector(p).normalize().multiply(-15.0))
        val pk2 = MoveEntityAbsolutePacket()
        pk2.eid = this.id
        pk2.x = pos.x.toFloat().toDouble()
        pk2.y = (pos.y - 30).toFloat().toDouble()
        pk2.z = pos.z.toFloat().toDouble()
        pk2.yaw = p.yaw.toFloat().toDouble()
        pk2.headYaw = p.yaw.toFloat().toDouble()
        pk2.pitch = p.pitch.toFloat().toDouble()
        p.dataPacket(pk2)
        p.dataPacket(this.permanentPacket)
        p.dataPacket(this.attributesPacket)
    }

    fun updateText(text: String) {
        this.metadata.putString(Entity.DATA_NAMETAG, text)
    }

    fun updateInfo() {
        val pk = SetEntityDataPacket()
        pk.eid = this.id
        pk.metadata = this.metadata

        this.attributesPacket.entries[0].setMaxValue(this.maxHealth.toFloat()).value = this.health.toFloat()
        this.attributesPacket.encode()
        this.attributesPacket.isEncoded = true

        plugin.server.batchPackets(players.values.toTypedArray(), arrayOf(this.attributesPacket, pk))
    }

    fun updateHealth() {
        this.attributesPacket.entries[0].setMaxValue(this.maxHealth.toFloat()).value = this.health.toFloat()
        this.attributesPacket.encode()
        this.attributesPacket.isEncoded = true

        Server.broadcastPacket(this.players.values, this.attributesPacket)
    }

    fun getDirectionVector(p: Player): Vector3 {
        val pitch = 1.5707963267948966
        val yaw = (p.getYaw() + this.random.nextRange(-10, 10).toDouble() + 90.0) * 3.141592653589793 / 180.0
        val x = Math.sin(pitch) * Math.cos(yaw)
        val z = Math.sin(pitch) * Math.sin(yaw)
        val y = Math.cos(pitch)
        return Vector3(x, y, z).normalize()
    }

    companion object {

        val WITHER_ID = 52
    }
}
