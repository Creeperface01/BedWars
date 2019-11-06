package com.creeperface.nukkit.bedwars.utils

object Color {
    private val BIT_MASK = 0xff

    val WHITE = 0xFFFFFF
    val SILVER = 0xC0C0C0
    val GRAY = 0x808080
    val BLACK = 0x000000
    val RED = 0xFF0000
    val MAROON = 0x800000
    val YELLOW = 0xFFFF00
    val OLIVE = 0x808000
    val LIME = 0x00FF00
    val GREEN = 0x008000
    val AQUA = 0x00FFFF
    val TEAL = 0x008080
    val BLUE = 0x0000FF
    val NAVY = 0x000080
    val FUCHSIA = 0xFF00FF
    val PURPLE = 0x800080
    val ORANGE = 0xFFA500

    fun toDecimal(bgr: Int): Int {
        val r = bgr shr 16 and BIT_MASK
        val g = bgr shr 8 and BIT_MASK
        val b = bgr shr 0 and BIT_MASK

        return hex2decimal(toHex(r, g, b))
    }

    private fun toHex(r: Int, g: Int, b: Int): String {
        return "#" + toBrowserHexValue(r) + toBrowserHexValue(g) + toBrowserHexValue(b)
    }

    private fun toBrowserHexValue(number: Int): String {
        val builder = StringBuilder(Integer.toHexString(number and 0xff))
        while (builder.length < 2) {
            builder.append("0")
        }
        return builder.toString().toUpperCase()
    }

    private fun hex2decimal(s: String): Int {
        var s = s
        val digits = "0123456789ABCDEF"
        s = s.toUpperCase()
        var value = 0
        for (element in s) {
            val d = digits.indexOf(element)
            value = 16 * value + d
        }
        return value
    }
}
