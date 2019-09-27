package com.creeperface.nukkit.bedwars.mysql

import cn.nukkit.scheduler.AsyncTask
import java.util.*

class QueryRefreshTask @JvmOverloads constructor(data: HashMap<String, String>, async: Boolean = true) : AsyncTask() {

//    internal var data: HashMap<String, String>? = null
//
//    init {
//        this.table = "servers"
//        this.data = data
//
//        if (MTCore.isShuttingDown || !async) {
//            onRun()
//        } else {
//            Server.getInstance().scheduler.scheduleAsyncTask(this)
//        }
//    }

    override fun onRun() {
//        try {
//            val players = data!!["players"]
//            val maxplayers = data!!["maxplayers"]
//            val line1 = data!!["line1"]
//            val line2 = data!!["line2"]
//            val line3 = data!!["line3"]
//            val line4 = data!!["line4"]
//
//            val text = "INSERT INTO servers ( id, players, maxplayers, line1, line2, line3, line4) VALUES ('" + data!!["id"] + "', '" + players + "', '" + maxplayers + "', '" + line1 + "', '" + line2 + "', '" + line3 + "', '" + line4 + "') ON DUPLICATE KEY UPDATE players = '" + players + "', maxplayers = '" + maxplayers + "', line1 = '" + line1 + "', line2 = '" + line2 + "', line3 = '" + line3 + "', line4 = '" + line4 + "'"
//            mysqli.use {
//                it.prepareStatement(text).use { it.executeUpdate() }
//            }
//        } catch (e: SQLException) {
//            e.printStackTrace()
//        }

    }
}
