package edu.byu.uapi.server.inputs

import edu.byu.uapi.server.scalars.EnumScalarConverterHelper
import edu.byu.uapi.server.scalars.builtinScalarTypeMap
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.scalars.ScalarType
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KClass

class DefaultTypeDictionary : TypeDictionary {
    private val scalarListeners = ConcurrentLinkedQueue<TypeDictionary.ScalarRegistrationListener>()
    override fun addScalarRegistrationListener(listener: TypeDictionary.ScalarRegistrationListener) {
        scalarListeners.add(listener)
        explicitScalarConverters.forEach { k, v -> listener.onRegister(k, v) }
        enumScalarCache.forEach { k, v -> listener.onRegister(k, v) }
    }

    override fun removeScalarRegistrationListener(listener: TypeDictionary.ScalarRegistrationListener) {
        scalarListeners.remove(listener)
    }

    private fun dispatchScalarRegistered(
        type: KClass<*>,
        scalarType: ScalarType<*>
    ) {
        scalarListeners.forEach { it.onRegister(type, scalarType) }
    }

    private val explicitScalarConverters = mapOf<KClass<*>, ScalarType<*>>() + builtinScalarTypeMap

    private val enumScalarCache = mutableMapOf<KClass<*>, ScalarType<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <Type : Any> scalarType(type: KClass<Type>): ScalarType<Type>? {
        if (type in explicitScalarConverters) {
            return explicitScalarConverters[type] as ScalarType<Type>
        }
        if (type.isEnum()) {
            return getOrCreateEnumScalar(type)
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun <Type : Any> getOrCreateEnumScalar(type: KClass<Type>): ScalarType<Type> {
        if (type in enumScalarCache) {
            return enumScalarCache[type]!! as ScalarType<Type>
        }
        val scalarType: ScalarType<Type> = EnumScalarConverterHelper.getEnumScalarConverter(type)
        val result = enumScalarCache.putIfAbsent(type, scalarType)
        if (result == null) {
            dispatchScalarRegistered(type, scalarType)
        }
        return scalarType
    }

    override fun isScalarType(type: KClass<*>): Boolean {
        return type in explicitScalarConverters || type.isEnum()
    }
}

private fun KClass<*>.isEnum() = this.java.isEnum
