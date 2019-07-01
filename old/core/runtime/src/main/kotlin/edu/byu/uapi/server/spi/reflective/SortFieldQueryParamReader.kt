package edu.byu.uapi.server.spi.reflective

import edu.byu.uapi.model.UAPIValueConstraints
import edu.byu.uapi.model.UAPIValueType
import edu.byu.uapi.server.scalars.EnumScalarType
import edu.byu.uapi.spi.SpecConstants.Collections.Query.KEY_SORT_PROPERTIES
import edu.byu.uapi.spi.input.ParamReadFailure
import edu.byu.uapi.spi.input.QueryParamMetadata
import edu.byu.uapi.spi.input.QueryParamReader
import edu.byu.uapi.spi.requests.QueryParams

class SortFieldQueryParamReader<T : Enum<T>>(
    val enumType: EnumScalarType<T>,
    val defaultValue: List<T>
) : QueryParamReader<List<T>, SortFieldQueryParamMetadata<T>> {

    private val values: List<SortFieldValue<T>> = getSortFieldValues(enumType)
    private val valueMap: Map<String, T> = values.map { it.queryParamValue to it.constant }.toMap()
    private val metadata = SortFieldQueryParamMetadata(values)

    override fun read(input: QueryParams): List<T> {
        val param = input[KEY_SORT_PROPERTIES]
            ?.asStringList()
        if (param.isNullOrEmpty()) {
            return defaultValue
        }
        return param.map { this.valueMap[it] ?: throw failInvalidValue() }
    }

    private fun failInvalidValue(): ParamReadFailure =
        ParamReadFailure(KEY_SORT_PROPERTIES, enumType.type, "Invalid sort property. Must be one of ${valueMap.keys}.")

    override fun describe(): SortFieldQueryParamMetadata<T> = metadata
}

private fun <T : Enum<T>> getSortFieldValues(enumType: EnumScalarType<T>): List<SortFieldValue<T>> {
    return enumType.enumConstants.map {
        SortFieldValue(it, enumType.renderToString(it))
    }
}

data class SortFieldQueryParamMetadata<T : Enum<T>>(
    val values: List<SortFieldValue<T>>
) : QueryParamMetadata {
    override val queryParams: List<QueryParamMetadata.Param> = listOf(QueryParamMetadata.Param(
        name = KEY_SORT_PROPERTIES,
        type = UAPIValueType.STRING,
        constraints = UAPIValueConstraints(enum = values.map { it.queryParamValue }.toSet()),
        repeatable = true
    ))
}

data class SortFieldValue<T : Enum<T>>(
    val constant: T,
    val queryParamValue: String
)
