package edu.byu.uapi.server.util

import edu.byu.uapi.server.inputs.create
import edu.byu.uapi.spi.UAPITypeError
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

inline fun <Type: Any, reified Interface: Any> Interface.extrapolateGenericType(genericName: String, thingToOverride: KCallable<*>): KClass<Type> {
    return try {
        DarkMagic.findSupertypeArgNamed(this::class, Interface::class, genericName)
    } catch (ex: DarkMagicException) {
        throw UAPITypeError.create(this::class, "Unable to extrapolate actual $genericName type. You should explicitly override `$thingToOverride`.", ex)
    }
}
