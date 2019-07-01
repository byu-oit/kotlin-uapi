package edu.byu.uapi.server.util

import edu.byu.uapi.spi.UAPITypeError
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.scalars.ScalarType
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

interface TypeIntrospector {
    @Throws(UAPITypeError::class)
    fun <Type : Any> introspectForCreate(
        type: KClass<Type>,
        typeDictionary: TypeDictionary
    ): IntrospectedCreateInfo<Type>
}


data class IntrospectedCreateInfo<Type : Any>(
    val type: KClass<Type>,
    val constructor: KFunction<Type>
)

sealed class CreateParameter {
    abstract val parameter: KParameter
    abstract val name: String
    abstract val type: KClass<*>

    val isNullable: Boolean
        get() = parameter.type.isMarkedNullable

    val hasDefault: Boolean
        get() = parameter.isOptional
}

data class ScalarCreateParameter(
    override val parameter: KParameter,
    override val name: String,
    val scalarType: ScalarType<*>
) : CreateParameter() {
    override val type: KClass<*> = scalarType.type
}

sealed class CollectionCreateParameter: CreateParameter() {
    abstract val containerType: ContainerType<*, *>
}

data class ScalarCollectionCreateParameter(
    override val parameter: KParameter,
    override val name: String,
    override val type: KClass<*>,
    override val containerType: ContainerType<*, *>,
    val scalarType: ScalarType<*>
) : CollectionCreateParameter()

data class ComplexCreateParameter(
    override val parameter: KParameter,
    override val name: String,
    override val type: KClass<*>,
    val parameters: List<CreateParameter>,
    val constructor: KFunction<*>
) : CreateParameter()

data class ComplexCollectionCreateParameter(
    override val parameter: KParameter,
    override val name: String,
    override val type: KClass<*>,
    override val containerType: ContainerType<*, *>,
    val parameters: List<CreateParameter>,
    val constructor: KFunction<*>
) : CollectionCreateParameter()

sealed class ContainerType<Type : Any, Nested : Any> {
    abstract fun createFrom(items: Iterable<Nested>): Type
}

class CollectionContainerType<Nested : Any> : ContainerType<Collection<Nested>, Nested>() {
    override fun createFrom(items: Iterable<Nested>): Collection<Nested> {
        return items.toList()
    }
}

class ListContainerType<Nested : Any> : ContainerType<List<Nested>, Nested>() {
    override fun createFrom(items: Iterable<Nested>): List<Nested> {
        return items.toList()
    }
}

class ArrayContainerType<Nested : Any>(
    private val nestedType: KClass<Nested>
) : ContainerType<Array<Nested>, Nested>() {
    override fun createFrom(items: Iterable<Nested>): Array<Nested> {
        return DarkMagic.createArrayOf(nestedType, items)
    }
}

class SetContainerType<Nested : Any> : ContainerType<Set<Nested>, Nested>() {
    override fun createFrom(items: Iterable<Nested>): Set<Nested> {
        return items.toSet()
    }
}

class EnumSetContainerType<Nested : Enum<Nested>> : ContainerType<Set<Nested>, Nested>() {
    override fun createFrom(items: Iterable<Nested>): Set<Nested> {
        return EnumSet.copyOf(items.toSet())
    }
}
