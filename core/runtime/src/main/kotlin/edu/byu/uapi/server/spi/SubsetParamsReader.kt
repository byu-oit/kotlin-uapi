package edu.byu.uapi.server.spi

import edu.byu.uapi.spi.SpecConstants.Collections.Query
import edu.byu.uapi.spi.input.QueryParamReader
import edu.byu.uapi.spi.input.SubsetParams
import edu.byu.uapi.spi.input.SubsetParamsMeta
import edu.byu.uapi.spi.requests.QueryParams
import edu.byu.uapi.spi.requests.asInt

class SubsetParamsReader(
    private val defaultSize: Int,
    private val maxSize: Int
) : QueryParamReader<SubsetParams, SubsetParamsMeta> {
    override fun read(input: QueryParams): SubsetParams {
        val size = input[Query.KEY_SUBSET_SIZE]
            ?.asInt()
            ?: defaultSize
        val startOffset = input[Query.KEY_SUBSET_START_OFFSET]
            ?.asInt()
            ?: Query.VALUE_DEFAULT_START_OFFSET

        // Enforce that sizes must not be larger than maxSize
        val boundedSize = if (size <= maxSize) size else maxSize

        return SubsetParams(
            startOffset,
            boundedSize
        )
    }

    override fun describe(): SubsetParamsMeta {
        return SubsetParamsMeta(defaultSize, maxSize)
    }
}
