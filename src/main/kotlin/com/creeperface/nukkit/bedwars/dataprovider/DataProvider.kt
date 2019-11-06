package com.creeperface.nukkit.bedwars.dataprovider

interface DataProvider {

    fun registerPlayer(player: String)

    fun getPlayerData()
}