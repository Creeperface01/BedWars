package bedwars.utils

import cn.nukkit.Player
import cn.nukkit.utils.TextFormat

object Lang {

    private val langs = mutableMapOf<String, String>()

    fun init(data: Map<String, Any>) {
        langs.clear()
        langs.putAll(data.mapValues { it.value.toString().replace('&', TextFormat.ESCAPE) })
    }

    fun translate(message: String, p: Player, vararg args: String): String {
        val translated = langs[message] ?: message

        if (args.isNotEmpty()) {
            val builder = StringBuilder(translated)

            args.forEachIndexed { index, arg ->
                builder.replaceAll("{%$index}", arg)
            }

            return builder.toString()
        }

        return translated
    }

    private fun StringBuilder.replaceAll(from: String, to: String) {
        var index = this.indexOf(from)

        while (index != -1) {
            this.replace(index, index + from.length, to)
            index += to.length
            index = this.indexOf(from, index)
        }
    }
}