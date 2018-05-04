package edu.byu.uapidsl.model

import com.google.common.collect.Multimap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor


data class FilterParamsModel<Type: Any> (
    val type: Introspectable<Type>
) {

}

interface QueryParamSetter<Type: Any> {
    fun instance(query: QueryParams)
}

class DataClassQueryParamSetter<Type: Any>(
    val type: KClass<Type>
): QueryParamSetter<Type> {

    init {
        if (!type.isData) {
            throw IllegalArgumentException("DataClassQueryParamSetter only works with data classes!")
        }
    }

    override fun instance(query: QueryParams) {
        type.primaryConstructor?.parameters?.first()?.kind
        TODO("not implemented")
    }
}

interface QueryParams: Map<String, String> {
}

