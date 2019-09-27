package bedwars.obj

import cn.nukkit.utils.Config
import java.util.*

object Language {

    private val data = HashMap<String, Map<String, String>>()
    private lateinit var current: Map<String, String>

    @Suppress("UNCHECKED_CAST")
    fun init(data: Map<String, Config>, language: String) {
        for ((key, value) in data) {
            Language.data[key] = value.all as Map<String, String>
        }

        current = (data[language] ?: error("")).all as Map<String, String>
    }

//    fun translate(message: String, p: Player, vararg args: String): String {
//        return translate(message, MTCore.getInstance().getPlayerData(p), *args)
//    }

//    fun translate(message: String, data: PlayerData, vararg args: String): String {
//        return translate(message, data.language, *args)
//    }

    fun translate(message: String, vararg args: String): String {
        var base: String = current[message]?.replace('&', '§') ?: return message

        for (i in args.indices) {
            base = base.replace("%$i", args[i])
        }

        return base
    }

//    fun getTranslations(msg: String, vararg args: String): HashMap<Int, String> {
//        val translations = HashMap<Int, String>()
//
//        for (i in Lang.getLanguages()) {
//            translations[i] = translate(msg, i, *args)
//        }
//
//        return translations
//    }
}
