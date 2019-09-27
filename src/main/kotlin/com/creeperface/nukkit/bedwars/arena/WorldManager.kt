package com.creeperface.nukkit.bedwars.arena

import cn.nukkit.Server
import org.apache.commons.io.FileUtils

import java.io.File
import java.io.IOException

object WorldManager {

    fun addWorld(name: String, id: String) {
        val from = File(Server.getInstance().dataPath + "/worlds/bedwars/" + name)
        val to = File(Server.getInstance().dataPath + "/worlds/" + name + "_" + id)

        try {
            FileUtils.copyDirectory(from, to)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun deleteWorld(name: String, id: String) {
        try {
            val directory = File(Server.getInstance().dataPath + "/worlds/" + name + "_" + id)
            FileUtils.deleteDirectory(directory)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun resetWorld(name: String, id: String) {
        deleteWorld(name, id)
        addWorld(name, id)
    }
}

