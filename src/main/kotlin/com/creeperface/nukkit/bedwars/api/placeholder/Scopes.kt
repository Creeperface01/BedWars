package com.creeperface.nukkit.bedwars.api.placeholder

import com.creeperface.nukkit.bedwars.api.arena.Arena
import com.creeperface.nukkit.bedwars.api.arena.Team
import com.creeperface.nukkit.placeholderapi.api.scope.Scope

object ArenaScope : Scope<Arena, ArenaScope>()

object TeamScope : Scope<Team, TeamScope>() {

    override val parent = ArenaScope

    fun getContext(context: Team): Context {
        return super.getContext(context, ArenaScope.getContext(context.arena))
    }
}