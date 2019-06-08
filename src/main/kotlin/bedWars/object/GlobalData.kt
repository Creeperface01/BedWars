package bedWars.`object`

import bedWars.arena.Arena
import cn.nukkit.Player

/**
 * Created by CreeperFace on 3.7.2017.
 */
class GlobalData(val player: Player) {

    var arena: Arena? = null

    val stats = Stats(player)
}
