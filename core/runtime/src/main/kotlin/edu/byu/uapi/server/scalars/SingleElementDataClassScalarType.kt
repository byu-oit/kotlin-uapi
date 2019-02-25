package edu.byu.uapi.server.scalars

import edu.byu.uapi.model.UAPIValueConstraints
import edu.byu.uapi.model.UAPIValueType
import edu.byu.uapi.server.util.DarkMagic
import edu.byu.uapi.server.util.DarkMagicException
import edu.byu.uapi.server.util.DarkerMagic
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.rendering.ScalarRenderer
import edu.byu.uapi.spi.scalars.ScalarFormat
import edu.byu.uapi.spi.scalars.ScalarType
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

class SingleElementDataClassScalarType<Type : Any, Wrapped : Any>(
    override val type: KClass<Type>,
    private val constructor: (Wrapped) -> Type,
    private val getter: (Type) -> Wrapped,
    private val wrappedScalar: ScalarType<Wrapped>
) : ScalarType<Type> {

    override val scalarFormat: ScalarFormat = wrappedScalar.scalarFormat

    override fun fromString(value: String): Type {
        return constructor(wrappedScalar.fromString(value))
    }

    override fun <S> render(value: Type, renderer: ScalarRenderer<S>): S {
        return wrappedScalar.render(getter(value), renderer)
    }

    override val valueType: UAPIValueType = wrappedScalar.valueType
    override val constraints: UAPIValueConstraints? = wrappedScalar.constraints


    companion object {
        fun <Type : Any> createIfPossible(
            type: KClass<Type>,
            typeDictionary: TypeDictionary
        ): ScalarType<Type>? {
            if (!type.isData) {
                return null
            }
            val ctor = type.primaryConstructor ?: return null
            val arg = ctor.parameters.singleOrNull() ?: return null
            val argType = try {
                DarkMagic.typeToClass(arg.type)
            } catch (ex: DarkMagicException) {
                return null
            }
            val wrapped = typeDictionary.scalarType(argType) ?: return null

            val getter = type.memberProperties.firstOrNull { it.name == arg.name && it.returnType == arg.type } ?: return null

            return DarkerMagic.createSingleElementDataClassScalarType(
                type,
                { ctor.call(it) },
                getter,
                wrapped
            )
        }
    }
}

