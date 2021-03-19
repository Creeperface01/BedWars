package com.creeperface.nukkit.bedwars.utils


import cn.nukkit.block.BlockAir
import cn.nukkit.inventory.Inventory
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import com.creeperface.nukkit.bedwars.BedWars
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.util.zip.ZipInputStream

object Items {

    val items = mutableMapOf<String, () -> Item>()

    val BRONZE by lazy {
        this["Bronze"]
    }

    val IRON by lazy {
        this["Iron"]
    }

    val GOLD by lazy {
        this["Gold"]
    }

    operator fun get(name: String) = items[name]?.invoke() ?: error("Item '$name' not found")

    fun init(plugin: BedWars) {
        val itemsDir = File(plugin.dataFolder, "items")
        itemsDir.mkdirs()

        val cacheFile = File(itemsDir, ".bw_cache")

        if (!cacheFile.exists()) {
            cacheFile.createNewFile()
            Files.setAttribute(cacheFile.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS)
        }

        val cache = try {
            JsonMapper().readValue<MutableSet<String>>(cacheFile)
        } catch (e: Exception) {
            logError("Error loading items cache", e)
            mutableSetOf()
        }

        val zip = ZipInputStream(plugin.getResource("items.zip"))

        while (true) {
            val entry = zip.nextEntry ?: break
            val itemName = entry.name.dropLast(4)

            if (cache.contains(itemName) || itemsDir.list()?.contains(entry.name) == true) {
                continue
            }

            val itemFile = File(itemsDir, entry.name)
            itemFile.createNewFile()
            itemFile.writer().use { writer ->
                val buffer = ByteArray(1024)
                while (true) {
                    val read = zip.read(buffer, 0, 1024)

                    if (read < 0) {
                        break
                    }

                    writer.write(String(buffer, 0, read))
                }
            }
        }

        itemsDir.listFiles { file -> file.name.endsWith(".yml") }?.forEach { file ->
            val itemName = file.nameWithoutExtension
            try {
                val itemData = Yaml().loadAs(file.inputStream(), Map::class.java)

                items[itemName] = { mapper.convertValue(itemData) }
            } catch (e: Exception) {
                logError("Error loading item '$itemName'", e)
            }
        }

        cache.addAll(items.keys)
        JsonMapper().writeValue(cacheFile, cache)
    }

    fun containsItem(inventory: Inventory, item: Item): Boolean {
        var count = 1.coerceAtLeast(item.getCount())

        for (i in 0 until inventory.size) {
            val item2 = inventory.getItem(i)

            if (item2.equals(item, true, false) && item2.getCount() > 0) {
                count -= item2.getCount()
            }

            if (count <= 0) {
                return true
            }
        }

        return false
    }

    fun removeItem(inv: Inventory, item: Item) {
        var count = item.getCount()

        for (i in 0 until inv.size) {
            val item1 = inv.getItem(i)

            if (item1.equals(item, true, false)) {
                if (count <= item1.count) {
                    item1.count -= count
                    inv.setItem(i, item1)
                    return
                } else {
                    count -= item1.getCount()
                    inv.setItem(i, ItemBlock(BlockAir()))
                }
            }
        }
    }
}
