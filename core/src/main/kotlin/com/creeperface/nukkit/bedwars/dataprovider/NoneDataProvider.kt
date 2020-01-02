package com.creeperface.nukkit.bedwars.dataprovider

import com.creeperface.nukkit.bedwars.api.arena.configuration.ArenaConfiguration
import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.data.Stats
import com.creeperface.nukkit.bedwars.api.data.provider.DataProvider

object NoneDataProvider : DataProvider {

    override suspend fun register(name: String, identifier: String) {

    }

    override suspend fun unregister(identifier: String) {

    }

    override suspend fun getData(identifier: String): Nothing? = null

    override suspend fun getDataByName(name: String): Nothing? = null

    override suspend fun saveData(identifier: String, data: Stats) {

    }

    override suspend fun loadArenas() = emptyMap<String, ArenaConfiguration>()

    override suspend fun syncArenas(arenas: Map<String, ArenaConfiguration>) {

    }

    override suspend fun loadMaps() = emptyMap<String, MapConfiguration>()

    override suspend fun syncMaps(maps: Map<String, MapConfiguration>) {

    }
}