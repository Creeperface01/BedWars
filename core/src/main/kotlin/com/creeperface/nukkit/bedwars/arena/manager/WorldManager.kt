package com.creeperface.nukkit.bedwars.arena.manager

import cn.nukkit.Server

import java.io.File
import java.io.IOException

object WorldManager {

    private fun addWorld(name: String, id: String) {
        val from = File(Server.getInstance().dataPath + "/worlds/bedwars/" + name)
        val to = File(Server.getInstance().dataPath + "/worlds/" + name + "_" + id)

        try {
            from.copyRecursively(to)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun deleteWorld(name: String, id: String) {
        try {
            val directory = File(Server.getInstance().dataPath + "/worlds/" + name + "_" + id)
            directory.deleteRecursively()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun resetWorld(name: String, id: String) {
        deleteWorld(name, id)
        addWorld(name, id)
    }
}

