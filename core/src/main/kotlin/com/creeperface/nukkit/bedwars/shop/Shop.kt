package com.creeperface.nukkit.bedwars.shop

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.utils.Config
import cn.nukkit.utils.ConfigSection
import com.creeperface.nukkit.bedwars.BedWars
import com.creeperface.nukkit.bedwars.api.shop.Shop
import com.creeperface.nukkit.bedwars.api.shop.ShopType
import com.creeperface.nukkit.bedwars.api.shop.ShopWindow
import com.creeperface.nukkit.bedwars.api.utils.Lang
import com.creeperface.nukkit.bedwars.arena.Arena
import com.creeperface.nukkit.bedwars.arena.Team
import com.creeperface.nukkit.bedwars.shop.generic.GenericMenuWindow
import com.creeperface.nukkit.bedwars.shop.generic.GenericOfferWindow
import com.creeperface.nukkit.bedwars.shop.inventory.MenuWindow
import com.creeperface.nukkit.bedwars.shop.inventory.Window
import com.creeperface.nukkit.bedwars.utils.*
import com.fasterxml.jackson.module.kotlin.convertValue
import java.io.File

class Shop(private val plugin: BedWars) : Shop {

    private val config: List<ConfigSection>

    override val type = plugin.configuration.shopType

    init {
        plugin.saveResource("shop.yml")

        config = Config(File(plugin.dataFolder, "shop.yml"), Config.YAML)
            .rootSection
            .getList("windows")
            .filterIsInstance<ConfigSection>()

        if (config.size > 255) {
            throw RuntimeException("The maximal window count per menu is 255")
        }
    }

    override fun open(player: Player, window: ShopWindow, type: ShopType) {
        when (type) {
            ShopType.INVENTORY -> player.openInventory(window.toInventory())
            ShopType.FORM -> plugin.shopFormManager.addWindow(player, window)
        }
    }

    //TODO: item texture names
    fun load(arena: Arena, team: Team): MenuWindow {
        if (config.isEmpty()) {
            return GenericMenuWindow.create(0, "Shop").inventory
        }

        fun loadWindow(parent: MenuWindow, section: ConfigSection, level: Int, id: Int): Window {
            val name = section.getString("name")
            val type = section.readEnum(ShopWindow.WindowType::class, "type")

            setConfigScopes(team.context)
            val iconItem: Item = when {
                section.containsKey("icon") -> {
                    mapper.convertValue(section.getSection("icon"))
                }
                type == ShopWindow.WindowType.OFFER -> {
                    mapper.convertValue(section.getSection("purchase_item"))
                }
                else -> {
                    throw RuntimeException("Menu window must contain icon entry")
                }
            }

//            val textureType = if (iconItem is ItemBlock) {
//                "blocks"
//            } else {
//                "items"
//            }

            //TODO: state name
//            section.getSection("icon").set(
//                "item_path",
//                "textures/$textureType/" + /*GlobalBlockPalette.getName(icon.item.id).substring(10) +*/ ".png"
//            )

            val icon = ShopWindow.WindowIcon(
                iconItem,
                section.getString("item_path")
            )

            if (type == ShopWindow.WindowType.OFFER) {
                val item = mapper.convertValue<Item>(section.getSection("purchase_item"))

                val cost = section.getList("cost").filterIsInstance<ConfigSection>().map {
                    mapper.convertValue<Item>(it)
                }

                return GenericOfferWindow(id, item, cost, name, icon, parent).inventory
            }
            resetConfigScopes()

            val windows = mutableMapOf<Int, ShopWindow>()

            val window = GenericMenuWindow(id, windows, name, icon, parent).inventory
            val nextLevel = level + 1

            if (nextLevel >= 5) { //not more than 5 levels
                throw RuntimeException("Maximal shop window nesting depth is 5")
            }

            val children = section.getList("children").filterIsInstance<ConfigSection>()

            if (children.size > 32) {
                throw RuntimeException("The maximal window count per menu is 32")
            }

            window.setWindows(children.mapIndexed { index, it ->
                index to loadWindow(window, it, nextLevel, id or (index shl (level * 5)))
            }.toMap())
            return window
        }

        val mainWindow = GenericMenuWindow.create(-1, "Shop").inventory

        mainWindow.setWindows(config.mapIndexed { index, section ->
            index to loadWindow(mainWindow, section, 0, index)
        }.toMap())

        return mainWindow
    }

    fun processTransaction(p: Player, item: Item, cost: Collection<Item>) {
        val inv = p.inventory

        for (costItem in cost) {
            if (!Items.containsItem(inv, costItem)) {
                p.sendMessage(Lang.HIGH_COST.translate(costItem.name))
                return
            }
        }

        if (!inv.canAddItem(item)) {
            p.sendMessage(Lang.FULL_INVENTORY.translate())
            return
        }

        for (costItem in cost) {
            Items.removeItem(inv, costItem)
        }

        inv.addItem(item)

        p.sendMessage(Lang.BUY.translatePrefix(if (item.hasCustomName()) item.customName else item.name))
    }
}