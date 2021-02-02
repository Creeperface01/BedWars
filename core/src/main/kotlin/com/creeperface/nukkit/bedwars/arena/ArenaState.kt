package com.creeperface.nukkit.bedwars.arena

import com.creeperface.nukkit.bedwars.arena.handler.ArenaGame
import com.creeperface.nukkit.bedwars.arena.handler.ArenaTeamSelect
import com.creeperface.nukkit.bedwars.arena.handler.ArenaVoting
import com.creeperface.nukkit.bedwars.utils.APIState

@Suppress("UNCHECKED_CAST")
object ArenaState {

    val VOTING = com.creeperface.nukkit.bedwars.api.arena.VOTING as APIState<ArenaVoting>
    val TEAM_SELECT = com.creeperface.nukkit.bedwars.api.arena.TEAM_SELECT as APIState<ArenaTeamSelect>
    val GAME = com.creeperface.nukkit.bedwars.api.arena.GAME as APIState<ArenaGame>
}