package edu.byu.uapi.server.util

import edu.byu.uapi.server.inputs.create
import edu.byu.uapi.spi.UAPITypeError
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import java.lang.reflect.Array as ReflectArray

object DarkMagic {
    fun <T : Any> findMatchingSupertype(me: KClass<out T>, type: KClass<T>): KType? {
        return me.allSupertypes.find { it.classifier == type }
    }

    fun <Super : Any, Expected : Any> findSupertypeArgNamed(
        me: KClass<out Super>,
        type: KClass<Super>,
        name: String
    ): KClass<Expected> {
        if (me.typeParameters.any { it.name == name }) {
            throw DarkMagicException("Can only extrapolate type parameter values for non-generic classes")
        }
        val supertype =
            findMatchingSupertype(me, type) ?: throw DarkMagicException("Unable to find supertype $type on $me.")
        val projection = getTypeParamNamed(supertype, name)
        return DarkMagic.getConcreteType(projection)
    }

    fun getTypeParamNamed(me: KType, name: String): KTypeProjection {
        val classifier = me.classifier ?: throw DarkMagicException("Type $me must be a valid Kotlin type")
        if (classifier !is KClass<*>) {
            throw DarkMagicException("Type $me must be a concrete class")
        }
        val paramIdx = classifier.typeParameters.indexOfFirst { it.name == name }
        if (paramIdx < 0) {
            throw DarkMagicException("Type $me does not have a type parameter named $name")
        }
        return me.arguments[paramIdx]
    }

    @Suppress("FoldInitializerAndIfToElvis")
    fun <T : Any> getConcreteType(projection: KTypeProjection): KClass<T> {
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

    @Suppress("UNCHECKED_CAST")
    fun <Type : Any> createArrayOf(type: KClass<Type>, items: Iterable<Type> = emptyList()): Array<Type> {
        val list = items.toList()
        val array: Array<Type> = ReflectArray.newInstance(type.java, list.size) as Array<Type>
        list.forEachIndexed { index, it -> array[index] = it }
        return array
    }

    private val collectionStar = Collection::class.starProjectedType

    fun isCollectionType(type: KType): Boolean {
        return type.isSubtypeOf(collectionStar)
    }

    fun isArrayType(type: KType): Boolean {
        val classifier = type.classifier ?: return false
        val klass = classifier as? KClass<*> ?: return false
        return klass.java.isArray
    }

    @Throws(UAPITypeError::class)
    fun typeToClass(type: KType): KClass<*> {
        val classifier = type.classifier ?: throw UAPITypeError.create(type, "Type must be representable in Kotlin")
        return classifier as? KClass<*> ?: throw UAPITypeError.create(
            classifier,
            "Type must be representable as a class"
        )
    }
}


class DarkMagicException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
