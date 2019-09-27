package bedwars.utils

import bedwars.obj.Team
import cn.nukkit.item.Item
import cn.nukkit.item.ItemFirework
import cn.nukkit.item.ItemFirework.FireworkExplosion
import cn.nukkit.item.ItemFirework.FireworkExplosion.ExplosionType
import cn.nukkit.utils.DyeColor
import java.util.*

object FireworkUtils {

    var BLUE: MutableList<Item> = ArrayList()
    var RED: MutableList<Item> = ArrayList()
    var GREEN: MutableList<Item> = ArrayList()
    var YELLOW: MutableList<Item> = ArrayList()

    fun init() {
        var blue = ItemFirework()
        var red = ItemFirework()
        var yellow = ItemFirework()
        var green = ItemFirework()

        //blue
        blue.addExplosion(FireworkExplosion()
                .addColor(DyeColor.BLUE)
                .addColor(DyeColor.LIGHT_BLUE)
                .addFade(DyeColor.GREEN)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.LARGE_BALL)
        )
        BLUE.add(blue)
        blue = ItemFirework()

        blue.addExplosion(FireworkExplosion()
                .addColor(DyeColor.BLUE)
                .addColor(DyeColor.LIGHT_BLUE)
                .addFade(DyeColor.GREEN)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.BURST)
        )
        BLUE.add(blue)
        blue = ItemFirework()

        blue.addExplosion(FireworkExplosion()
                .addColor(DyeColor.BLUE)
                .addColor(DyeColor.LIGHT_BLUE)
                .addFade(DyeColor.GREEN)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.STAR_SHAPED)
        )
        BLUE.add(blue)
        blue = ItemFirework()

        blue.addExplosion(FireworkExplosion()
                .addColor(DyeColor.BLUE)
                .addColor(DyeColor.CYAN)
                .addFade(DyeColor.LIGHT_BLUE)
                .addFade(DyeColor.LIME)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.LARGE_BALL)
        )
        BLUE.add(blue)
        red = ItemFirework()

        //red

        red.addExplosion(FireworkExplosion()
                .addColor(DyeColor.RED)
                .addColor(DyeColor.ORANGE)
                .addFade(DyeColor.YELLOW)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.LARGE_BALL)
        )
        RED.add(red)
        red = ItemFirework()

        red.addExplosion(FireworkExplosion()
                .addColor(DyeColor.RED)
                .addColor(DyeColor.ORANGE)
                .addFade(DyeColor.YELLOW)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.BURST)
        )
        RED.add(red)
        red = ItemFirework()

        red.addExplosion(FireworkExplosion()
                .addColor(DyeColor.RED)
                .addColor(DyeColor.ORANGE)
                .addFade(DyeColor.YELLOW)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.STAR_SHAPED)
        )
        RED.add(red)
        red = ItemFirework()

        red.addExplosion(FireworkExplosion()
                .addColor(DyeColor.RED)
                .addColor(DyeColor.PURPLE)
                .addFade(DyeColor.ORANGE)
                .addFade(DyeColor.YELLOW)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.LARGE_BALL)
        )
        RED.add(red)
        yellow = ItemFirework()

        //yellow
        yellow.addExplosion(FireworkExplosion()
                .addColor(DyeColor.YELLOW)
                .addColor(DyeColor.LIME)
                .addFade(DyeColor.PINK)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.LARGE_BALL)
        )
        YELLOW.add(yellow)
        yellow = ItemFirework()

        yellow.addExplosion(FireworkExplosion()
                .addColor(DyeColor.YELLOW)
                .addColor(DyeColor.LIME)
                .addFade(DyeColor.PINK)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.BURST)
        )
        YELLOW.add(yellow)
        yellow = ItemFirework()

        yellow.addExplosion(FireworkExplosion()
                .addColor(DyeColor.YELLOW)
                .addColor(DyeColor.LIME)
                .addFade(DyeColor.PINK)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.STAR_SHAPED)
        )
        YELLOW.add(yellow)
        yellow = ItemFirework()

        yellow.addExplosion(FireworkExplosion()
                .addColor(DyeColor.YELLOW)
                .addColor(DyeColor.PINK)
                .addFade(DyeColor.ORANGE)
                .addFade(DyeColor.LIME)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.LARGE_BALL)
        )
        YELLOW.add(yellow)
        green = ItemFirework()

        //green
        green.addExplosion(FireworkExplosion()
                .addColor(DyeColor.GREEN)
                .addColor(DyeColor.LIME)
                .addFade(DyeColor.YELLOW)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.LARGE_BALL)
        )
        GREEN.add(green)
        green = ItemFirework()

        green.addExplosion(FireworkExplosion()
                .addColor(DyeColor.GREEN)
                .addColor(DyeColor.LIME)
                .addFade(DyeColor.YELLOW)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.BURST)
        )
        GREEN.add(green)
        green = ItemFirework()

        green.addExplosion(FireworkExplosion()
                .addColor(DyeColor.GREEN)
                .addColor(DyeColor.LIME)
                .addFade(DyeColor.YELLOW)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.STAR_SHAPED)
        )
        GREEN.add(green)
        green = ItemFirework()

        green.addExplosion(FireworkExplosion()
                .addColor(DyeColor.LIME)
                .addColor(DyeColor.GREEN)
                .addFade(DyeColor.LIGHT_BLUE)
                .addFade(DyeColor.PINK)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.LARGE_BALL)
        )
        GREEN.add(green)
    }

    fun of(team: Team): List<Item> {
        when (team.id) {
            1 -> return BLUE
            2 -> return RED
            3 -> return YELLOW
            4 -> return GREEN
        }

        throw RuntimeException("Invalid team id given")
    }
}
