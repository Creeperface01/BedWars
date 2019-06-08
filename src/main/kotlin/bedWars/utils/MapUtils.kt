package bedWars.utils

import cn.nukkit.math.Vector3
import cn.nukkit.utils.Config
import cn.nukkit.utils.ConfigSection
import java.util.*

/**
 * Created by CreeperFace on 2.7.2017.
 */
object MapUtils {

    fun loadMap(cfg: Config): Map<String, Vector3> {
        val data = HashMap<String, Vector3>()

        for ((key1, value) in cfg.getSection("teams")) {
            val team = (Integer.parseInt(key1) + 1).toString() + ""

            val section = value as ConfigSection

            for (dataEntry in section.entries) {
                var key = dataEntry.key

                if (key.equals("bed1", ignoreCase = true)) {
                    key = "bed"
                }

                data[team + key] = readVector3(dataEntry.key, section)
            }
        }

        return data
    }

    private fun readVector3(key: String, section: ConfigSection): Vector3 {
        val section1 = section.getSection(key)

        val vector = Vector3()
        vector.x = section1.getDouble("x")
        vector.y = section1.getDouble("y")
        vector.z = section1.getDouble("z")

        return vector
    }

    fun offsetMap(map: HashMap<String, Vector3>, x: Double, z: Double) {
        for ((key, v) in map) {
            if (key == "sign") continue

            v.setComponents(v.x + x, v.y, v.z + z)
        }
    }
}
