@file:Suppress("HasPlatformType")

package com.creeperface.nukkit.bedwars.utils

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.command.CommandSender
import cn.nukkit.event.Event
import cn.nukkit.event.EventPriority
import cn.nukkit.event.Listener
import cn.nukkit.inventory.Inventory
import cn.nukkit.item.Item
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.level.format.FullChunk
import cn.nukkit.plugin.MethodEventExecutor
import cn.nukkit.plugin.Plugin
import cn.nukkit.utils.DyeColor
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.shop.ShopMenuWindow
import com.creeperface.nukkit.bedwars.api.shop.ShopOfferWindow
import com.creeperface.nukkit.bedwars.api.shop.ShopWindow
import com.creeperface.nukkit.bedwars.shop.inventory.*
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.google.common.base.CaseFormat
import org.joor.Reflect
import java.sql.ResultSet
import java.util.*
import kotlin.collections.ArrayList
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaMethod

const val DEMO = true

inline fun demo(action: () -> Unit) {
    if(DEMO) {
        action()
    }
}

inline fun production(action: () -> Unit) {
    if(!DEMO) {
        action()
    }
}

typealias TF = TextFormat

private val RGB_CONVERTER = arrayOf(
        1908001,
        11546150,
        6192150,
        8606770,
        3949738,
        8991416,
        1481884,
        10329495,
        4673362,
        15961002,
        8439583,
        16701501,
        3847130,
        13061821,
        16351261,
        16383998
)

val DyeColor.rgb: Int
    get() = RGB_CONVERTER[this.ordinal]

operator fun TextFormat.plus(any: Any) = this.toString() + any

val Block.blockEntity: BlockEntity?
    get() = this.level.getBlockEntity(this)

val Block.fullChunk: FullChunk
    get() = this.level.getChunk(this.chunkX, this.chunkZ)

@ExperimentalContracts
fun requirePlayer(sender: CommandSender, action: (() -> Unit)? = null) {
    contract {
        returns() implies (sender is Player)
    }

    if (sender !is Player) {
        action?.invoke()
    }
}

fun ResultSet.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    val metadata = this.metaData

    for (i in 1..metadata.columnCount) {
        map[metadata.getColumnName(i)] = this.getObject(i)
    }

    return map
}

fun String?.ucFirst(): String {
    if (this.isNullOrEmpty()) {
        return String()
    }

    val chars = this.toCharArray()
    chars[0] = chars[0].toUpperCase()
    return String(chars)
}

fun Item.setCountR(count: Int): Item {
    val item = this.clone()
    item.setCount(count)
    return item
}

fun Item.addEnchantment(id: Int, lvl: Int): Item {
    val e = Enchantment.get(id)
    e.setLevel(lvl, false)
    this.addEnchantment(e)

    return this
}

fun <T : Any> KClass<T>.initClass(vararg params: Any): T {
    this.objectInstance?.let { return it }

    this.constructors.forEach const@{ constructor ->
        val values = ArrayList<Any>(constructor.parameters.size)
        val localParams = params.toList()

        constructor.parameters.forEach param@{ param ->
            val classifier = param.type.classifier

            if (classifier is KClass<*>) {
                localParams.forEach { lp ->
                    if (lp::class.isSubclassOf(classifier)) {
                        values.add(lp)
                        return@param
                    }
                }
            }

            return@const
        }

        if (values.size != constructor.parameters.size) {
            return@const
        }

        constructor.call(*values.toTypedArray())
    }

    throw RuntimeException("Callable constructor not found")
}

val Player.identifier: String
    get() = BedWars.instance.configuration.playerIdentifier.get(this).toString()

fun <T> MutableCollection<T>.merge(list: Collection<T>): Collection<T> {
    this.addAll(list)
    return this
}

@Suppress("UNCHECKED_CAST")
fun <K, V> MutableMap<K, V>.deepMerge(map: Map<K, V>): Map<K, V> {
    map.forEach { (k, v) ->
        val v1 = this[k]

        if (v is Map<*, *> && v1 is MutableMap<*, *>) {
            (v1 as MutableMap<Any, Any>).deepMerge(v as Map<Any, Any>)
            return@forEach
        }

        if (v is Collection<*> && v1 is MutableCollection<*>) {
            (v1 as MutableCollection<Any>).merge(v as Collection<Any>)
            return@forEach
        }

        this[k] = v
    }

    return this
}

operator fun Appendable.plusAssign(str: String) {
    this.append(str)
}

operator fun TextFormat.plus(str: String) = this.toString() + str

fun ShopWindow.toInventory(): Window {
    return when (this) {
        is Window -> this
        is ShopMenuWindow -> MenuWindow(this)
        is ShopOfferWindow -> OfferWindow(this)
        else -> throw RuntimeException("Unknown window class '${this::class.qualifiedName}'")
    }
}

fun <T> Optional<T>.unwrap(): T? = orElse(null)

fun Player.openInventory(inv: Inventory) {
    val id = this.getWindowId(inv)

//    if (id != -1) {
//        Reflect.on(this).get<MutableMap<Inventory, Int>>("windows").remove(inv)
//    }
//
//    this.addWindow(inv)

    if (id >= 0) {
        inv.onOpen(this)
    } else {
        this.addWindow(inv)
    }
}

fun Player.openShopInventory(inv: Window) {
    openInventory(inv)
//    val windows = Reflect.on(this).get<MutableMap<Inventory, Int>>("windows")
//    val top = this.topWindow.unwrap()
//
//    val id = if(top is ShopInventory) {
//        this.getWindowId(top)
//    } else {
//        null
//    }
//
//    if(top != null) {
//        windows.remove(top)
//    }
//
//    if(id != null) {
//        windows[inv] = id
//    } else {
//        this.addWindow(inv)
//    }
//
//    inv.sendContents(this)
}

@Suppress("NOTHING_TO_INLINE")
inline fun logAlert(message: String) =
    Reflect.on(BedWars.instance).call("getLogger").call("alert", TextFormat.YELLOW + message)

@Suppress("NOTHING_TO_INLINE")
inline fun logInfo(message: String) =
    Reflect.on(BedWars.instance).call("getLogger").call("info", TextFormat.GRAY + message)

@Suppress("NOTHING_TO_INLINE")
inline fun logWarning(message: String) =
    Reflect.on(BedWars.instance).call("getLogger").call("warning", TextFormat.YELLOW + message)

@Suppress("NOTHING_TO_INLINE")
inline fun logError(message: String) = Reflect.on(BedWars.instance).call("getLogger").call("error", TextFormat.RED + message)

@Suppress("NOTHING_TO_INLINE")
inline fun logError(message: String, t: Throwable) = Reflect.on(BedWars.instance).call("getLogger").call("error", TextFormat.RED + message, t)

private val jacksonMapper = JsonMapper.builder()
        .addModule(Jdk8Module())
        .addModule(JavaTimeModule())
        .build()

fun <T : Any> KClass<T>.fromJson(data: String): T = jacksonMapper.readValue(data, this.java)

fun <T : Any> KClass<T>.fromMap(data: Map<String, *>): T = jacksonMapper.convertValue(data, this.java)

fun Any.toJson(): String = jacksonMapper.writeValueAsString(this)

internal val configuration: Configuration
    get() = BedWars.instance.configuration

fun String.snakeToCamelCase() = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this)

fun String.camelToSnakeCase() = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, this)

fun Player.sendPermissionMessage() {
    val msg = this.server.language.translate(TranslationContainer("commands.generic.permission"))
    this.sendMessage(TextFormat.RED + msg)
}

fun Listener.register(plugin: Plugin) {
    val pm = plugin.server.pluginManager
    this::class.declaredFunctions.forEach {
        try {
            val paramClazz = it.parameters.single().type.classifier as? KClass<*> ?: return@forEach

            paramClazz.java.let { clazz ->
                val event = clazz.asSubclass(Event::class.java)

                pm.registerEvent(event, this, EventPriority.NORMAL, MethodEventExecutor(it.javaMethod), plugin)
            }
        } catch (ignore: Exception) {

        }
    }
}

fun Listener.register(plugin: Plugin, func: KFunction<*>, priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false) {
    val pm = plugin.server.pluginManager
    val paramClazz = func.parameters.single().type.classifier as KClass<*>

    paramClazz.java.let { clazz ->
        val event = clazz.asSubclass(Event::class.java)

        pm.registerEvent(event, this, priority, MethodEventExecutor(func.javaMethod), plugin, ignoreCancelled)
    }
}

fun String.replaceColors() = this.replace('&', '§')

fun printBWLogo() {
    logInfo("")
    logInfo("${TF.RED}██████╗ ███████╗██████╗ ${TF.WHITE}██╗    ██╗ █████╗ ██████╗ ███████╗")
    logInfo("${TF.RED}██╔══██╗██╔════╝██╔══██╗${TF.WHITE}██║    ██║██╔══██╗██╔══██╗██╔════╝")
    logInfo("${TF.RED}██████╔╝█████╗  ██║  ██║${TF.WHITE}██║ █╗ ██║███████║██████╔╝███████╗")
    logInfo("${TF.RED}██╔══██╗██╔══╝  ██║  ██║${TF.WHITE}██║███╗██║██╔══██║██╔══██╗╚════██║")
    logInfo("${TF.RED}██████╔╝███████╗██████╔╝${TF.WHITE}╚███╔███╔╝██║  ██║██║  ██║███████║")
    logInfo("${TF.RED}╚═════╝ ╚══════╝╚═════╝ ${TF.WHITE} ╚══╝╚══╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝")
    logInfo("")
    logInfo("                     ${TF.RED}Bed${TF.WHITE}Wars enabled                      ")
}