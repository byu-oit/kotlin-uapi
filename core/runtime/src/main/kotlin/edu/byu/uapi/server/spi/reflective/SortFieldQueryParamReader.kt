package edu.byu.uapi.server.spi.reflective

import edu.byu.uapi.server.scalars.EnumScalarType
import edu.byu.uapi.spi.SpecConstants.Collections.Query.KEY_SORT_PROPERTIES
import edu.byu.uapi.spi.functional.Failure
import edu.byu.uapi.spi.functional.asSuccess
import edu.byu.uapi.spi.functional.useFailure
import edu.byu.uapi.spi.input.ParamReadFailure
import edu.byu.uapi.spi.input.ParamReadResult
import edu.byu.uapi.spi.input.QueryParamMetadata
import edu.byu.uapi.spi.input.QueryParamReader
import edu.byu.uapi.spi.requests.QueryParams
import edu.byu.uapi.spi.scalars.ScalarFormat

class SortFieldQueryParamReader<T : Enum<T>>(
    val enumType: EnumScalarType<T>,
    val defaultValue: List<T>
) : QueryParamReader<List<T>, SortFieldQueryParamMetadata<T>> {

    private val values: List<SortFieldValue<T>> = getSortFieldValues(enumType)
    private val valueMap: Map<String, T> = values.map { it.queryParamValue to it.constant }.toMap()
    private val metadata = SortFieldQueryParamMetadata(values)

    override fun read(input: QueryParams): ParamReadResult<List<T>> {
        val param = input[KEY_SORT_PROPERTIES]
            ?.asStringList()
            ?.useFailure { return it }
        if (param.isNullOrEmpty()) {
            return defaultValue.asSuccess()
        }
        return param.map { this.valueMap[it] ?: return@read failInvalidValue() }.asSuccess()
    }

    private fun failInvalidValue(): Failure<ParamReadFailure> = Failure(
        ParamReadFailure(KEY_SORT_PROPERTIES, enumType.type, "Invalid sort property. Must be one of ${valueMap.keys}.")
    )

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
        format = ScalarFormat.STRING.asEnum(values.map { it.queryParamValue }),
        repeatable = true
    ))
}

data class SortFieldValue<T : Enum<T>>(
    val constant: T,
    val queryParamValue: String
)
