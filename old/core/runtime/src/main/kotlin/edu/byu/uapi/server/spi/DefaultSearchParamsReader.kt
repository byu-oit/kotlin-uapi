package edu.byu.uapi.server.spi

import edu.byu.uapi.model.UAPIListSearchFeature
import edu.byu.uapi.spi.UAPITypeError
import edu.byu.uapi.spi.input.ParamReadFailure
import edu.byu.uapi.spi.input.SearchParams
import edu.byu.uapi.spi.input.SearchParamsMeta
import edu.byu.uapi.spi.input.SearchParamsMeta.Companion.SEARCH_CONTEXT_KEY
import edu.byu.uapi.spi.input.SearchParamsMeta.Companion.SEARCH_TEXT_KEY
import edu.byu.uapi.spi.input.SearchParamsReader
import edu.byu.uapi.spi.introspection.IntrospectionContext
import edu.byu.uapi.spi.requests.QueryParams
import edu.byu.uapi.spi.scalars.ScalarType

class DefaultSearchParamsReader<SearchContext : Enum<SearchContext>> private constructor(
    private val contextType: ScalarType<SearchContext>,
    searchFields: Map<SearchContext, Collection<String>>
) : SearchParamsReader<SearchContext> {

    override fun read(input: QueryParams): SearchParams<SearchContext>? {
        val textParam = input[SEARCH_TEXT_KEY]
        val contextParam = input[SEARCH_CONTEXT_KEY]
        return if (textParam == null && contextParam == null) {
            null
        } else if (textParam != null && contextParam != null) {
            val context = contextParam.asScalar(contextType)
            val text = textParam.asString()
            SearchParams(
                context = context,
                text = text
            )
        } else {
            throw ParamReadFailure(
                SEARCH_CONTEXT_KEY,
                SearchParams::class,
                "If one of $SEARCH_TEXT_KEY and $SEARCH_CONTEXT_KEY is specified, the other must also be specified."
            )
        }
    }

    private val meta: SearchParamsMeta = SearchParamsMeta(
        searchFields.mapKeys { contextType.renderToString(it.key) }
    )

    override fun describe(): SearchParamsMeta = meta

    override fun introspect(context: IntrospectionContext): UAPIListSearchFeature {
        return UAPIListSearchFeature(
            searchContextsAvailable = meta.contextFields.mapValues { it.value.toList().sorted() }
        )
    }

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

