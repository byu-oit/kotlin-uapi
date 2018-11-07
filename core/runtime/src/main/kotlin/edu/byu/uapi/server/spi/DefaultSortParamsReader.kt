package edu.byu.uapi.server.spi

import edu.byu.uapi.server.scalars.EnumScalarType
import edu.byu.uapi.spi.UAPITypeError
import edu.byu.uapi.spi.input.SortOrder
import edu.byu.uapi.spi.input.SortParams
import edu.byu.uapi.spi.input.SortParamsMeta
import edu.byu.uapi.spi.input.SortParamsReader
import edu.byu.uapi.spi.requests.QueryParams

class DefaultSortParamsReader<SortProperty : Enum<SortProperty>> private constructor(
    private val propertyType: EnumScalarType<SortProperty>,
    private val sortOrderType: EnumScalarType<SortOrder>,
    private val defaultProperties: List<SortProperty>,
    private val defaultOrder: SortOrder
) : SortParamsReader<SortProperty> {

    override fun read(input: QueryParams): SortParams<SortProperty> {
        val props = input[SortParamsMeta.SORT_PROPERTIES_KEY]
            ?.asScalarList(propertyType)
            ?: defaultProperties
        val order = input[SortParamsMeta.SORT_ORDER_KEY]
            ?.asScalar(sortOrderType)
            ?: defaultOrder

        return SortParams(
            fields = props,
            order = order
        )
    }

    private val meta: SortParamsMeta = SortParamsMeta(
        propertyType.enumValues,
        defaultProperties.map { propertyType.renderToString(it) },
        defaultOrder
    )

    override fun describe(): SortParamsMeta = meta

    companion object {
        @Throws(UAPITypeError::class)
        fun <SortProperty : Enum<SortProperty>> create(
            propertyType: EnumScalarType<SortProperty>,
            sortOrderType: EnumScalarType<SortOrder>,
            defaultProperties: List<SortProperty>,
            defaultOrder: SortOrder
        ): DefaultSortParamsReader<SortProperty> {
            return DefaultSortParamsReader(propertyType, sortOrderType, defaultProperties, defaultOrder)
        }
    }

}

