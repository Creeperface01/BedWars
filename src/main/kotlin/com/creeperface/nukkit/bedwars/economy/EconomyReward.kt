package com.creeperface.nukkit.bedwars.economy

import com.creeperface.nukkit.bedwars.api.data.Stat
import com.creeperface.nukkit.bedwars.api.economy.EconomyProvider

class EconomyReward(
        val stat: Stat,
        val currency: EconomyProvider.Currency,
        val amount: Double
)