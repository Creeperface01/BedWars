package com.creeperface.nukkit.bedwars.utils

import cn.nukkit.item.Item
import cn.nukkit.item.ItemFirework
import cn.nukkit.item.ItemFirework.FireworkExplosion
import cn.nukkit.item.ItemFirework.FireworkExplosion.ExplosionType
import cn.nukkit.utils.DyeColor
import com.creeperface.nukkit.bedwars.arena.Team
import java.util.*

object FireworkUtils {

    private val BLUE: MutableList<Item> = ArrayList()
    private val RED: MutableList<Item> = ArrayList()
    private val GREEN: MutableList<Item> = ArrayList()
    private val YELLOW: MutableList<Item> = ArrayList()

    private val lookup = EnumMap<DyeColor, List<Item>>(DyeColor::class.java)

    init {
        lookup[DyeColor.BLACK] = emptyList()
        lookup[DyeColor.RED] = RED
        lookup[DyeColor.GREEN] = GREEN
        lookup[DyeColor.BROWN] = emptyList()
        lookup[DyeColor.BLUE] = BLUE
        lookup[DyeColor.PURPLE] = emptyList()
        lookup[DyeColor.CYAN] = emptyList()
        lookup[DyeColor.LIGHT_GRAY] = emptyList()
        lookup[DyeColor.GRAY] = emptyList()
        lookup[DyeColor.PINK] = emptyList()
        lookup[DyeColor.LIME] = emptyList()
        lookup[DyeColor.YELLOW] = YELLOW
        lookup[DyeColor.LIGHT_BLUE] = emptyList()
        lookup[DyeColor.MAGENTA] = emptyList()
        lookup[DyeColor.ORANGE] = emptyList()
        lookup[DyeColor.WHITE] = emptyList()
    }

    fun init() {

        //blue
        var blue = ItemFirework()
        blue.addExplosion(
            FireworkExplosion()
                .addColor(DyeColor.BLUE)
                .addColor(DyeColor.LIGHT_BLUE)
                .addFade(DyeColor.GREEN)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.LARGE_BALL)
        )
        BLUE.add(blue)
        blue = ItemFirework()

        blue.addExplosion(
            FireworkExplosion()
                .addColor(DyeColor.BLUE)
                .addColor(DyeColor.LIGHT_BLUE)
                .addFade(DyeColor.GREEN)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.BURST)
        )
        BLUE.add(blue)
        blue = ItemFirework()

        blue.addExplosion(
            FireworkExplosion()
                .addColor(DyeColor.BLUE)
                .addColor(DyeColor.LIGHT_BLUE)
                .addFade(DyeColor.GREEN)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.STAR_SHAPED)
        )
        BLUE.add(blue)
        blue = ItemFirework()

        blue.addExplosion(
            FireworkExplosion()
                .addColor(DyeColor.BLUE)
                .addColor(DyeColor.CYAN)
                .addFade(DyeColor.LIGHT_BLUE)
                .addFade(DyeColor.LIME)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.LARGE_BALL)
        )
        BLUE.add(blue)
        var red = ItemFirework()

        //red

        red.addExplosion(
            FireworkExplosion()
                .addColor(DyeColor.RED)
                .addColor(DyeColor.ORANGE)
                .addFade(DyeColor.YELLOW)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.LARGE_BALL)
        )
        RED.add(red)
        red = ItemFirework()

        red.addExplosion(
            FireworkExplosion()
                .addColor(DyeColor.RED)
                .addColor(DyeColor.ORANGE)
                .addFade(DyeColor.YELLOW)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.BURST)
        )
        RED.add(red)
        red = ItemFirework()

        red.addExplosion(
            FireworkExplosion()
                .addColor(DyeColor.RED)
                .addColor(DyeColor.ORANGE)
                .addFade(DyeColor.YELLOW)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.STAR_SHAPED)
        )
        RED.add(red)
        red = ItemFirework()

        red.addExplosion(
            FireworkExplosion()
                .addColor(DyeColor.RED)
                .addColor(DyeColor.PURPLE)
                .addFade(DyeColor.ORANGE)
                .addFade(DyeColor.YELLOW)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.LARGE_BALL)
        )
        RED.add(red)
        var yellow = ItemFirework()

        //yellow
        yellow.addExplosion(
            FireworkExplosion()
                .addColor(DyeColor.YELLOW)
                .addColor(DyeColor.LIME)
                .addFade(DyeColor.PINK)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.LARGE_BALL)
        )
        YELLOW.add(yellow)
        yellow = ItemFirework()

        yellow.addExplosion(
            FireworkExplosion()
                .addColor(DyeColor.YELLOW)
                .addColor(DyeColor.LIME)
                .addFade(DyeColor.PINK)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.BURST)
        )
        YELLOW.add(yellow)
        yellow = ItemFirework()

        yellow.addExplosion(
            FireworkExplosion()
                .addColor(DyeColor.YELLOW)
                .addColor(DyeColor.LIME)
                .addFade(DyeColor.PINK)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.STAR_SHAPED)
        )
        YELLOW.add(yellow)
        yellow = ItemFirework()

        yellow.addExplosion(
            FireworkExplosion()
                .addColor(DyeColor.YELLOW)
                .addColor(DyeColor.PINK)
                .addFade(DyeColor.ORANGE)
                .addFade(DyeColor.LIME)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.LARGE_BALL)
        )
        YELLOW.add(yellow)
        var green = ItemFirework()

        //green
        green.addExplosion(
            FireworkExplosion()
                .addColor(DyeColor.GREEN)
                .addColor(DyeColor.LIME)
                .addFade(DyeColor.YELLOW)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.LARGE_BALL)
        )
        GREEN.add(green)
        green = ItemFirework()

        green.addExplosion(
            FireworkExplosion()
                .addColor(DyeColor.GREEN)
                .addColor(DyeColor.LIME)
                .addFade(DyeColor.YELLOW)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.BURST)
        )
        GREEN.add(green)
        green = ItemFirework()

        green.addExplosion(
            FireworkExplosion()
                .addColor(DyeColor.GREEN)
                .addColor(DyeColor.LIME)
                .addFade(DyeColor.YELLOW)
                .setTrail(true)
                .setFlicker(true)
                .type(ExplosionType.STAR_SHAPED)
        )
        GREEN.add(green)
        green = ItemFirework()

        green.addExplosion(
            FireworkExplosion()
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
        lookup[team.color]?.let { return it }

        throw RuntimeException("Invalid team id given")
    }
}
