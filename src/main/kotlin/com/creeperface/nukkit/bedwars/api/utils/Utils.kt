package com.creeperface.nukkit.bedwars.api.utils

import com.creeperface.nukkit.bedwars.api.arena.Arena
import com.creeperface.nukkit.bedwars.api.arena.Team
import com.creeperface.nukkit.bedwars.api.arena.configuration.MutableConfiguration
import com.creeperface.nukkit.bedwars.api.placeholder.ArenaScope
import com.creeperface.nukkit.bedwars.api.placeholder.TeamScope
import com.creeperface.nukkit.placeholderapi.api.scope.Scope
import java.time.Instant
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

typealias ArenaContext = Scope<Arena, ArenaScope>.Context
typealias TeamContext = Scope<Team, TeamScope>.Context

operator fun <T, E : Enum<E>> Array<T>.get(index: Enum<E>) = this[index.ordinal]

operator fun <T, E : Enum<E>> Array<T>.set(index: Enum<E>, value: T) {
    this[index.ordinal] = value
}

inline fun <reified T, reified V> MutableConfiguration.watching(defaultValue: V? = null) =
    watch<T, V>(this, defaultValue)

inline fun <reified T, reified V> watch(conf: MutableConfiguration, defaultValue: V? = null) =
    object : ReadWriteProperty<T, V> {

        var value: V

        init {
            if (null !is V && defaultValue == null) {
                error("Cannot set default value to null of nonnull property")
            }

            value = defaultValue as V
        }

        override fun getValue(thisRef: T, property: KProperty<*>): V = value

        override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
            conf.lastModification = Instant.now()
        }
    }