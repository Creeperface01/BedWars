package bedWars.utils

import bedWars.arena.Arena
import cn.nukkit.utils.TextFormat

/**
 * Created by CreeperFace on 5. 11. 2016.
 */
class BossBarUtil(private val plugin: Arena) {

    var mainLine = ""
    var other = ""

    private val lineOffset = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
    private var mainLineOffset = ""

    fun updateBar(time: Int) {
        if (plugin.game == 0) {
            if (plugin.starting) {
                mainLine = TextFormat.GOLD.toString() + "                           Lobby" + TextFormat.GRAY + " | " + TextFormat.WHITE + " Time to start: " + time
            } else {
                updateVotes()
                mainLine = TextFormat.GRAY.toString() + "                                              Welcome to bedWars!"
                //mainLine = recalculateLineOffset() + mainLine;
                this.plugin.bossBar.maxHealth = 50
                this.plugin.bossBar.health = 50
            }
        } else if (plugin.game == 1 && !plugin.ending) {
            mainLine = TextFormat.GOLD.toString() + "Game " + getTimeString(time)
            mainLine = recalculateLineOffset() + mainLine
        } else if (plugin.ending) {
            this.plugin.bossBar.maxHealth = 20
            other = ""
            mainLine = TextFormat.GOLD.toString() + "Total time: " + getTimeString(plugin.task.gameTime) + TextFormat.GRAY + " | " + TextFormat.GREEN + "Restarting in " + time
            this.plugin.bossBar.health = time
        } else {
            return
        }

        this.update()
    }

    fun updateVotes() {
        val vm = this.plugin.votingManager
        //$this->plugin->plugin->getServer()->getLogger()->info("{$vm->stats[1]} {$vm->stats[2]} {$vm->stats[3]}");
        val votes = vm.currentTable

        var tip = "                                                                                          §8Voting §f| §6/vote <map>"/*
                + "\n                                                 §b[1] §8" + votes[0] + " §c» §a" + vm.stats.get(votes[0]) + " Hlasu"
                + "\n                                                 §b[2] §8" + votes[1] + " §c» §a" + vm.stats.get(votes[1]) + " Hlasu"
                + "\n                                                 §b[3] §8" + votes[2] + " §c» §a" + vm.stats.get(votes[2]) + " Hlasu";*/

        for (i in votes.indices) {
            tip += "\n                                                                                      §b[" + (i + 1) + "] §8" + votes[i] + " §c» §a" + vm.stats[votes[i]] + " Hlasu"
        }
        //$this->plugin->plugin->getServer()->getLogger()->info("{$vm->stats[1]} {$vm->stats[2]} {$vm->stats[3]}");
        this.other = tip

        /*for (Player p : this.plugin.getAllPlayers().values()) {
            p.sendPopup(tip);
        }   */                     //    |

        update()
    }

    fun updateTeamStats() {
        //int[] nex = new int[]{this.plugin.getTeam(1).getNexus().getHealth(), this.plugin.getTeam(2).getNexus().getHealth(), this.plugin.getTeam(3).getNexus().getHealth(), this.plugin.getTeam(4).getNexus().getHealth()};

        other = plugin.gameStatus

        update()
    }

    private fun update() {
        this.plugin.bossBar.updateText(mainLine + lineOffset + other)
        this.plugin.bossBar.updateInfo()
    }

    private fun getTimeString(time: Int): String {
        val hours = time / 3600L
        val minutes = (time - hours * 3600L) / 60L
        val seconds = time.toLong() - hours * 3600L - minutes * 60L
        return String.format(TextFormat.WHITE.toString() + "%02d" + TextFormat.GRAY + ":"
                + TextFormat.WHITE + "%02d" + TextFormat.GRAY + ":"
                + TextFormat.WHITE + "%02d", hours, minutes, seconds).replace("-", "")
    }

    private fun recalculateLineOffset(): String {
        var maxLength = 0

        for (line in other.split("\n".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()) {
            maxLength = Math.max(maxLength, line.length)
        }

        val firstLength = mainLine.length

        if (maxLength < firstLength) {
            return ""
        }

        mainLineOffset = String(CharArray(((maxLength - firstLength) / 2 * 1.4).toInt())).replace("\u0000", " ")
        return mainLineOffset
    }
}
