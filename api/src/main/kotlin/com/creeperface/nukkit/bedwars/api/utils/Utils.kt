package com.creeperface.nukkit.bedwars.api.utils

operator fun <T, E : Enum<E>> Array<T>.get(index: Enum<E>) = this[index.ordinal]

operator fun <T, E : Enum<E>> Array<T>.set(index: Enum<E>, value: T) {
    this[index.ordinal] = value
}