package com.creeperface.nukkit.bedwars.dataprovider

import com.creeperface.nukkit.bedwars.api.data.Stats
import com.creeperface.nukkit.bedwars.api.data.provider.DataProvider

object NoneDataProvider : DataProvider {

    override suspend fun register(name: String, identifier: String) {
        throw UnsupportedOperationException()
    }

    override suspend fun unregister(identifier: String) {
        throw UnsupportedOperationException()
    }

    override suspend fun getData(identifier: String): Stats {
        throw UnsupportedOperationException()
    }

    override suspend fun getDataByName(name: String): Stats {
        throw UnsupportedOperationException()
    }

    override suspend fun saveData(identifier: String, data: Stats) {
        throw UnsupportedOperationException()
    }
}