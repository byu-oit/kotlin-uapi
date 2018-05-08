package edu.byu.uapidsl.model

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

fun <Type: Any> getPathIdentifierModel(type: KClass<Type>): PathIdentifierModel<Type> {
    val introspectable = Introspectable(type)
    return when (type) {
        KClass<Type>::isData -> CompoundPathIdentifierModel(introspectable)
        else -> SimplePathIdentifierModel(introspectable)
    }
}

sealed class PathIdentifierModel<Type: Any> {
    abstract val type: Introspectable<Type>
    abstract val isCompound: Boolean
    abstract val names: List<String>
}

data class SimplePathIdentifierModel<Type: Any> (
    override val type: Introspectable<Type>
): PathIdentifierModel<Type>() {
    override val isCompound = false

    override val names = listOf("id")
}

data class CompoundPathIdentifierModel<Type: Any> (
    override val type: Introspectable<Type>
): PathIdentifierModel<Type>() {
    override val isCompound: Boolean = true

    override val names by lazy { type.uapiPropNames }
}

interface DeserializationContext {

}

data class FilterParamsModel<Type: Any> (
    val type: Introspectable<Type>
) {

}

interface QueryParamSetter<Type: Any> {
    fun instance(query: QueryParams, context: DeserializationContext)
}

class DataClassQueryParamSetter<Type: Any>(
    val type: KClass<Type>
): QueryParamSetter<Type> {

    init {
        if (!type.isData) {
            throw IllegalArgumentException("DataClassQueryParamSetter only works with data classes!")
        }
    }

    override fun instance(query: QueryParams, context: DeserializationContext) {
        type.primaryConstructor?.parameters?.first()?.kind
        TODO("not implemented")
    }
}

interface QueryParams: Map<String, String> {
}

