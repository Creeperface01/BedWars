package com.creeperface.nukkit.bedwars.task

import cn.nukkit.InterruptibleThread
import java.util.*

class QueryThread : Thread(), InterruptibleThread {

    private var data = HashMap<String, String>()

    override fun run() {
//        while (!BedWars.instance.shuttingDown) {
//            this.data = BedWars.instance!!.refreshQuery(true)
//
//            QueryRefreshTask(data, false)
//
//            try {
//                Thread.sleep(1000)
//            } catch (e: InterruptedException) {
//                //ignore
//            }
//
//        }
    }
}
