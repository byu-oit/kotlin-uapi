package edu.byu.uapi.server.spi

import edu.byu.uapi.spi.UAPITypeError
import edu.byu.uapi.spi.functional.Failure
import edu.byu.uapi.spi.functional.Success
import edu.byu.uapi.spi.functional.useFailure
import edu.byu.uapi.spi.input.*
import edu.byu.uapi.spi.input.SearchParamsMeta.Companion.SEARCH_CONTEXT_KEY
import edu.byu.uapi.spi.input.SearchParamsMeta.Companion.SEARCH_TEXT_KEY
import edu.byu.uapi.spi.requests.QueryParams
import edu.byu.uapi.spi.scalars.ScalarType

class DefaultSearchParamsReader<SearchContext : Enum<SearchContext>> private constructor(
    private val contextType: ScalarType<SearchContext>,
    searchFields: Map<SearchContext, Collection<String>>
) : SearchParamsReader<SearchContext> {

    override fun read(input: QueryParams): ParamReadResult<SearchParams<SearchContext>?> {
        val textParam = input[SEARCH_TEXT_KEY]
        val contextParam = input[SEARCH_CONTEXT_KEY]
        return if (textParam == null && contextParam == null) {
            Success(null)
        } else if (textParam != null && contextParam != null) {
            val context = contextParam.asScalar(contextType).useFailure { return it }
            val text = textParam.asString().useFailure { return it }
            Success(SearchParams(
                context = context,
                text = text
            ))
        } else {
            Failure(ParamReadFailure(
                SEARCH_CONTEXT_KEY,
                SearchParams::class,
                "If one of $SEARCH_TEXT_KEY and $SEARCH_CONTEXT_KEY is specified, the other must also be specified."
            ))
        }
    }

    private val meta: SearchParamsMeta = SearchParamsMeta(
        searchFields.mapKeys { contextType.renderToString(it.key) }
    )

    override fun describe(): SearchParamsMeta = meta

    companion object {
        @Throws(UAPITypeError::class)
        fun <SearchContext: Enum<SearchContext>> create(
            contextType: ScalarType<SearchContext>,
            searchFields: Map<SearchContext, Collection<String>>
        ): DefaultSearchParamsReader<SearchContext> {
            return DefaultSearchParamsReader(contextType, searchFields)
        }
    }
}

