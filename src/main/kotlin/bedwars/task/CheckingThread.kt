package bedwars.task

import cn.nukkit.Server

class CheckingThread(id: Long) : Runnable {

    internal var id: Long = 0

    init {
        this.id = id
    }

    override fun run() {
        while (Server.getInstance().isRunning) {
            val stacks = Thread.getAllStackTraces()

            for ((thread, value) in stacks) {

                if (thread.id != id) {
                    continue
                }

                var stackTrace = ""

                for (element in value) {
                    stackTrace += element.toString() + "\n"
                }

                println(stackTrace)
            }

            try {
                Thread.sleep(20000)
            } catch (e: InterruptedException) {
                println("interrupted thread")
                return
            }

        }
    }
}
