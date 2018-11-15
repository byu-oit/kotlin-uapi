package edu.byu.uapi.server.inputs

import edu.byu.uapi.server.scalars.EnumScalarConverterHelper
import edu.byu.uapi.server.scalars.builtinScalarTypeMap
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.scalars.ScalarType
import kotlin.reflect.KClass

class DefaultTypeDictionary: TypeDictionary {

    private val explicitScalarConverters = mapOf<KClass<*>, ScalarType<*>>() + builtinScalarTypeMap

    private val enumScalarCache = mutableMapOf<KClass<*>, ScalarType<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <Type : Any> scalarType(type: KClass<Type>): ScalarType<Type>? {
        if (explicitScalarConverters.containsKey(type)) {
            return explicitScalarConverters[type] as ScalarType<Type>
        }
        if (type.isEnum()) {
            return enumScalarCache.getOrPut(type) {
                EnumScalarConverterHelper.getEnumScalarConverter(type)
            } as ScalarType<Type>
        }
        return null
    }

    override fun isScalarType(type: KClass<*>): Boolean {
        return explicitScalarConverters.containsKey(type) || type.isEnum()
    }
}

private fun KClass<*>.isEnum() = this.java.isEnum
