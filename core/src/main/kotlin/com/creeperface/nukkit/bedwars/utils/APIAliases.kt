package com.creeperface.nukkit.bedwars.utils

import com.creeperface.nukkit.bedwars.api.arena.Arena
import com.creeperface.nukkit.bedwars.api.arena.State
import com.creeperface.nukkit.bedwars.api.arena.Team
import com.creeperface.nukkit.bedwars.api.shop.Shop
import com.creeperface.nukkit.bedwars.api.shop.ShopMenuWindow
import com.creeperface.nukkit.bedwars.api.shop.ShopOfferWindow
import com.creeperface.nukkit.bedwars.api.shop.ShopWindow

typealias APITeam = Team
typealias APIArena = Arena
typealias APIShop = Shop
typealias APIMenuWindow = ShopMenuWindow
typealias APIOfferWindow = ShopOfferWindow
typealias APIShopWindow = ShopWindow
typealias APIState<T> = State<T>