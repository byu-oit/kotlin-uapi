package edu.byu.uapi.server.spi

import edu.byu.uapi.server.scalars.EnumScalarType
import edu.byu.uapi.spi.dictionary.MaybeTypeFailure
import edu.byu.uapi.spi.functional.Success
import edu.byu.uapi.spi.functional.orDefault
import edu.byu.uapi.spi.functional.useFailure
import edu.byu.uapi.spi.input.*
import edu.byu.uapi.spi.requests.QueryParams

class DefaultSortParamsReader<SortProperty : Enum<SortProperty>> private constructor(
    private val propertyType: EnumScalarType<SortProperty>,
    private val sortOrderType: EnumScalarType<SortOrder>,
    private val defaultProperties: List<SortProperty>,
    private val defaultOrder: SortOrder
) : SortParamsReader<SortProperty> {

    override fun read(input: QueryParams): ParamReadResult<SortParams<SortProperty>> {
        val props = input[SortParamsMeta.SORT_PROPERTIES_KEY]
            ?.asScalarList(propertyType)
            .orDefault(defaultProperties)
            .useFailure { return it }
        val order = input[SortParamsMeta.SORT_ORDER_KEY]
            ?.asScalar(sortOrderType)
            .orDefault(defaultOrder)
            .useFailure { return it }

        return Success(SortParams(
            fields = props,
            order = order
        ))
    }

    private val meta: SortParamsMeta = SortParamsMeta(
        propertyType.enumValues,
        defaultProperties.map { propertyType.renderToString(it) },
        defaultOrder
    )

    override fun describe(): SortParamsMeta = meta

    companion object {
        fun <SortProperty : Enum<SortProperty>> create(
            propertyType: EnumScalarType<SortProperty>,
            sortOrderType: EnumScalarType<SortOrder>,
            defaultProperties: List<SortProperty>,
            defaultOrder: SortOrder
        ): MaybeTypeFailure<DefaultSortParamsReader<SortProperty>> {
            return Success(
                DefaultSortParamsReader(propertyType, sortOrderType, defaultProperties, defaultOrder)
            )
        }
    }

}

