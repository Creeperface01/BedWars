package com.creeperface.nukkit.bedwars.api.arena.configuration

import java.time.Instant

interface MutableConfiguration {

    var lastModification: Instant

    companion object {

        fun get(defaultValue: Instant) = object : MutableConfiguration {

            override var lastModification: Instant = defaultValue

        }
    }
}