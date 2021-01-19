package com.creeperface.nukkit.bedwars.dataprovider

import com.creeperface.nukkit.bedwars.api.arena.configuration.ArenaConfiguration
import com.creeperface.nukkit.bedwars.api.arena.configuration.MapConfiguration
import com.creeperface.nukkit.bedwars.api.arena.configuration.MutableConfiguration
import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.api.data.Stats
import com.creeperface.nukkit.bedwars.api.data.provider.DataProvider
import com.creeperface.nukkit.bedwars.api.utils.set
import com.creeperface.nukkit.bedwars.utils.Configuration
import com.creeperface.nukkit.bedwars.utils.fromJson
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import org.apache.http.client.utils.URIBuilder
import org.bson.Document
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

internal class MongoDBDataProvider(private val configuration: Configuration) : DataProvider {

    private lateinit var client: MongoClient
    private lateinit var collection: MongoCollection<Document>
    private lateinit var arenasCollection: MongoCollection<Document>
    private lateinit var mapsCollection: MongoCollection<Document>

    override fun init() {
        val mongoConf = configuration.mongo

        val builder = URIBuilder("mongodb://${mongoConf.user}:${mongoConf.password}@${mongoConf.host}")
        mongoConf.options.forEach { (key, value) ->
            builder.addParameter(key, value.toString())
        }

        client = MongoClients.create(builder.build().toString())
        val database = client.getDatabase(mongoConf.database)

        collection = database.getCollection(STATS)
        arenasCollection = database.getCollection(ARENAS)
        mapsCollection = database.getCollection(MAPS)
    }

    override fun deinit() {
        client.close()
    }

    override suspend fun register(name: String, identifier: String) {
        val doc = Document()
            .append("identifier", identifier)
            .append("name", name)

        collection.insertOne(doc)
    }

    override suspend fun unregister(identifier: String) {
        collection.deleteOne(Filters.eq("identifier", identifier))
    }

    override suspend fun getData(identifier: String): Stats? {
        val result = collection.find(Filters.eq("identifier", identifier)).first() ?: return null

        return parseResult(result)
    }

    override suspend fun getDataByName(name: String): Stats? {
        val result = collection.find(Filters.eq("name", name)).first() ?: return null

        return parseResult(result)
    }

    private fun parseResult(result: Document): Stats {
        val stats = Array(Stat.values().size) { 0 }
        result.forEach { key, value ->
            try {
                val stat = Stat.valueOf(key)
                stats[stat] = value.toString().toInt()
            } catch (e: IllegalArgumentException) {

            }
        }

        return Stats(stats)
    }

    override suspend fun saveData(identifier: String, data: Stats) {
        val doc = Document()

        Stat.values().forEach {
            doc.append(it.name.toLowerCase(), data.getDelta(it))
        }

        collection.updateOne(
            Filters.eq("identifier", identifier), Document()
                .append("\$inc", doc)
        )
    }

    private inline fun <reified T : MutableConfiguration> sync(
        data: MutableMap<String, T>,
        collection: MongoCollection<Document>
    ) {
        collection.find().forEach {
            val name = it.getString("name")
            val local = data[name]

            val modifyTime = it.getDate("last_update").toInstant()

            if (local == null || modifyTime > local.lastModification) {
                data[name] = T::class.fromJson(it.getString("data"))
            }
        }
    }

    override suspend fun syncArenas(arenas: MutableMap<String, ArenaConfiguration>) {
        sync(arenas, arenasCollection)
    }

    override suspend fun syncMaps(maps: MutableMap<String, MapConfiguration>) {
        sync(maps, mapsCollection)
    }

    companion object {

        const val STATS = "bedwars_stats"
        const val MAPS = "bedwars_maps"
        const val ARENAS = "bedwars_arenas"
    }
}