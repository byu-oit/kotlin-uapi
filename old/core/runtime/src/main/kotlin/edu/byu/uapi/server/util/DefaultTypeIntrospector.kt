package edu.byu.uapi.server.util

import edu.byu.uapi.server.inputs.create
import edu.byu.uapi.spi.UAPITypeError
import edu.byu.uapi.spi.dictionary.TypeDictionary
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.valueParameters

class DefaultTypeIntrospector: TypeIntrospector {
    @Throws(UAPITypeError::class)
    override fun <Type : Any> introspectForCreate(
        type: KClass<Type>,
        typeDictionary: TypeDictionary
    ): IntrospectedCreateInfo<Type> {
        val ctor = type.primaryConstructor ?: throw UAPITypeError.create(type, "Missing primary constructor")

        val analyzedParams = ctor.valueParameters
            .map { introspectCreateParam(it, typeDictionary) }

        TODO("not implemented")

    }

    private val collectionStar = Collection::class.starProjectedType

    private fun introspectCreateParam(param: KParameter, typeDictionary: TypeDictionary): CreateParameter {
        val type = param.type
        if (DarkMagic.isCollectionType(type) || DarkMagic.isArrayType(type)) {
            return introspectCollection(param, typeDictionary)
        }
        val klass = DarkMagic.typeToClass(type)
        if (typeDictionary.isScalarType(klass)) {
            return introspectScalarParam(param, klass, typeDictionary)
        }
        return introspectComplexCreateParam(param, typeDictionary)
    }

    private fun introspectComplexCreateParam(
        param: KParameter,
        typeDictionary: TypeDictionary
    ): ComplexCreateParameter {
        TODO("not implemented")
    }

    private fun introspectScalarParam(
        param: KParameter,
        klass: KClass<*>,
        typeDictionary: TypeDictionary
    ): ScalarCreateParameter {
        TODO("not implemented")
    }

    private fun introspectCollection(
        param: KParameter,
        typeDictionary: TypeDictionary
    ): CreateParameter {
        TODO()
    }

}

