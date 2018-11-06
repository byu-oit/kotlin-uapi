package edu.byu.uapi.server.util

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

object DarkMagic {
    fun findMatchingSupertype(me: KClass<*>, type: KClass<*>): KType? {
        val star = type.starProjectedType
        return me.supertypes.find { it.isSubtypeOf(star) }
    }

    @Suppress("FoldInitializerAndIfToElvis")
    fun <T: Any> getConcreteType(projection: KTypeProjection): KClass<T> {
        val type = projection.type ?: fail(projection, "must not be a '*' projection")
        if (projection.variance !== KVariance.INVARIANT) {
            fail(projection, "must be an invariant type (not 'in' or 'out')")
        }
        val actual = type.classifier ?: fail(projection, "must be a valid Kotlin type")
        if (actual !is KClass<*>) {
            fail(projection, "must be a concrete class")
        }
        @Suppress("UNCHECKED_CAST")
        return actual as KClass<T>
    }

    private fun fail(projection: KTypeProjection, message: String): Nothing {
        throw DarkMagicException("Type projection $projection $message")
    }

}

class DarkMagicException(message: String, cause: Throwable? = null): RuntimeException(message, cause)
