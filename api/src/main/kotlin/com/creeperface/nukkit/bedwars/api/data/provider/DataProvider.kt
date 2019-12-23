package com.creeperface.nukkit.bedwars.api.data.provider

import com.creeperface.nukkit.bedwars.api.data.Stats

interface DataProvider {

    fun init() {

    }

    fun deinit() {

    }

    suspend fun register(name: String, identifier: String)

    suspend fun unregister(identifier: String)

    suspend fun getData(identifier: String): Stats?

    suspend fun getDataByName(name: String): Stats?

    suspend fun saveData(identifier: String, data: Stats)
}