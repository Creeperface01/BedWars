@file:Suppress("UNCHECKED_CAST")

package com.creeperface.nukkit.bedwars.utils

import java.lang.invoke.MethodHandles
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.reflect.*
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

val finalAccess = if (System.getProperty("java.version").split('.')[0].toInt() <= 9) {
    val modifiersField = Field::class.java.getDeclaredField("modifiers")
    modifiersField.isAccessible = true

    { field: Field ->
        modifiersField.set(field, field.modifiers and Modifier.FINAL.inv())
    }
} else {
    val lookup = MethodHandles.privateLookupIn(Field::class.java, MethodHandles.lookup())
    val modifiers = lookup.findVarHandle(Field::class.java, "modifiers", Int::class.java)

    val action = { field: Field ->
        modifiers.set(field, field.modifiers and Modifier.FINAL.inv())
    }

    action
}

private fun <T : Any> KClass<T>.findProperty(name: String): KProperty1<T, *>? {
    val f = this.declaredMemberProperties.find { it.name == name }

    if (f != null) {
        return f
    }

    superclasses.forEach {
        it.findProperty(name)?.let { fn ->
            return fn as KProperty1<T, *>
        }
    }

    return null
}

private fun <T : Any> KClass<T>.findFunction(name: String, args: Array<out Any?>): KFunction<*>? {
    val f = this.declaredFunctions.find {
        if (it.name != name) {
            return@find false
        }

        val params = it.parameters
        if ((params.size - 1) != args.size) {
            return@find false
        }

        params.drop(1).forEachIndexed { index, param ->
            val arg = args[index] ?: return@forEachIndexed

            val argClass = arg::class

            val paramClassifier = param.type.classifier ?: return@forEachIndexed

            if (paramClassifier is KClass<*>) {
                if (!paramClassifier.isSuperclassOf(argClass)) {
                    return@find false
                }
            }
        }

        return@find true
    }

    if (f != null) {
        return f
    }

    superclasses.forEach {
        it.findFunction(name, args)?.let { fn ->
            return fn
        }
    }

    return null
}

inline fun <reified T : Any> T.setProperty(name: String, value: Any?) =
    T::class.accessProperty(name).setValue(this, value)

inline fun <reified T : Any, R> T.getProperty(name: String): R = T::class.accessProperty(name).getValue(this)

inline fun <reified T : Any> T.callAny(name: String, vararg args: Any?): Any = call(name, *args)

inline fun <reified T : Any, R> T.call(name: String, vararg args: Any?): R = this::class.call(this, name, *args)

fun <T : Any> KClass<T>.accessProperty(name: String) = property(name).accessible()

fun <T : Any> KClass<T>.property(name: String): KProperty1<T, *> =
    this.findProperty(name)!!

fun <T : Any, R> KClass<out T>.call(receiver: T, name: String, vararg args: Any?): R {
    return this.findFunction(name, args)!!.accessible().call(receiver, *args) as R
}

fun <T, K : KCallable<T>> K.accessible(): K {
    this.isAccessible = true
    return this
}

fun <R, T, P, K : KProperty1<T, P>> K.getValue(receiver: T): R = this.get(receiver) as R

fun <T, P, K : KProperty1<T, P>> K.setValue(receiver: T, value: Any?) {
    if (!this.isFinal && this is KMutableProperty<*>) {
        this.setter.call(receiver, value)
        return
    }

    val field = this.javaField!!
    field.isAccessible = true

    finalAccess(field)
    field.set(receiver, value)
}