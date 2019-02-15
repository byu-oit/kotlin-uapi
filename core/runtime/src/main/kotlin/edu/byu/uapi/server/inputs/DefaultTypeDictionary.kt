package edu.byu.uapi.server.inputs

import edu.byu.uapi.server.util.DarkerMagic
import edu.byu.uapi.server.scalars.builtinScalarTypeMap
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.scalars.ScalarType
import edu.byu.uapi.utility.collections.TypeMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KClass

class DefaultTypeDictionary : TypeDictionary {
    private val scalarListeners = ConcurrentLinkedQueue<TypeDictionary.ScalarRegistrationListener>()
    override fun addScalarRegistrationListener(listener: TypeDictionary.ScalarRegistrationListener) {
        scalarListeners.add(listener)
        knownScalarTypes.forEach { k, v -> listener.onRegister(k, v) }
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

    private fun dispatchScalarUnregistered(
        type: KClass<*>,
        scalarType: ScalarType<*>
    ) {
        scalarListeners.forEach { it.onUnregister(type, scalarType) }
    }

    private val knownScalarTypes = TypeMap.create(builtinScalarTypeMap)

    private val enumScalarCache = mutableMapOf<KClass<*>, ScalarType<*>>()

    override fun registerScalarType(scalarType: ScalarType<*>) {
        val old = knownScalarTypes.put(scalarType.type, scalarType)
        if (old != null) {
            dispatchScalarUnregistered(old.type, old)
        }
        dispatchScalarRegistered(scalarType.type, scalarType)
    }

    override fun unregisterScalarType(scalarType: ScalarType<*>) {
        val removed = knownScalarTypes.remove(scalarType.type, scalarType)
        if (removed) {
            dispatchScalarUnregistered(scalarType.type, scalarType)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <Type : Any> scalarType(type: KClass<Type>): ScalarType<Type>? {
        val found = knownScalarTypes.getMatching(type)
        if (found != null) {
            return found as ScalarType<Type>
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
        val scalarType: ScalarType<Type> = DarkerMagic.getEnumScalarConverter(type)
        val result = enumScalarCache.putIfAbsent(type, scalarType)
        if (result == null) {
            dispatchScalarRegistered(type, scalarType)
        }
        return scalarType
    }

    override fun isScalarType(type: KClass<*>): Boolean {
        return knownScalarTypes.hasMatching(type) || type.isEnum()
    }
}

private fun KClass<*>.isEnum() = this.java.isEnum
