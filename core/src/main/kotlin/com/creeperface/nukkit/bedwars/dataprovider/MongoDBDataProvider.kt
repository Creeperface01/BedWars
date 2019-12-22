package com.creeperface.nukkit.bedwars.dataprovider

import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.api.data.Stats
import com.creeperface.nukkit.bedwars.api.data.provider.DataProvider
import com.creeperface.nukkit.bedwars.api.utils.set
import com.creeperface.nukkit.bedwars.utils.Configuration
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import org.apache.http.client.utils.URIBuilder
import org.bson.Document

internal class MongoDBDataProvider(private val configuration: Configuration) : DataProvider {

    private lateinit var client: MongoClient
    private lateinit var collection: MongoCollection<Document>

    override fun init() {
        val mongoConf = configuration.mongo

        val builder = URIBuilder("mongodb://${mongoConf.user}:${mongoConf.password}@${mongoConf.host}")
        mongoConf.options.forEach { (key, value) ->
            builder.addParameter(key, value.toString())
        }

        client = MongoClients.create(builder.build().toString())
        val database = client.getDatabase(mongoConf.database)

        collection = database.getCollection("bedwars")
    }

    override fun deinit() {
        client.close()
    }

    override suspend fun register(name: String, identifier: String) {
        val doc = Document()
                .append("identifier", identifier)
                .append("name", name)
                .append("kills", 0)
                .append("deaths", 0)
                .append("wins", 0)
                .append("losses", 0)
                .append("beds", 0)
                .append("place", 0)
                .append("break", 0)
                .append("games", 0)

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

    }
}