package edu.byu.uapi.server.spi

import edu.byu.uapi.model.UAPIListFeatureModel
import edu.byu.uapi.server.resources.list.asIntrospectionLocation
import edu.byu.uapi.spi.UAPITypeError
import edu.byu.uapi.spi.input.*
import edu.byu.uapi.spi.introspection.IntrospectionContext
import edu.byu.uapi.spi.introspection.IntrospectionLocation
import edu.byu.uapi.spi.requests.QueryParams
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability

class DefaultListParamReader<Params : ListParams> private constructor(
    private val paramsType: KClass<Params>,
    private val search: SearchParamsReader<*>?,
    private val filter: FilterParamsReader<*>?,
    private val sort: SortParamsReader<*>?,
    private val subset: SubsetParamsReader?,
    private val constructor: KFunction<Params>,
    private val parameterMap: Map<KParameter, QueryParamReader<*, *>>
) : ListParamReader<Params> {

    override fun introspect(context: IntrospectionContext): UAPIListFeatureModel {
        return context.withLocation(paramsType.asIntrospectionLocation()) {
            val model = UAPIListFeatureModel(
                search = search?.let { context.introspect(it) },
                filters = filter?.let { context.introspect(it) }.orEmpty(),
                sorting = sort?.let { context.introspect(it) },
                subset = subset?.let { context.introspect(it) }
            )

            val expectedParams = mutableSetOf<String>()

            if (search != null) expectedParams += ListParams.WithSearch.FIELD_NAME
            if (sort != null) expectedParams += ListParams.WithSort.FIELD_NAME
            if (subset != null) expectedParams += ListParams.WithSubset.FIELD_NAME
            if (filter != null) expectedParams += ListParams.WithFilters.FIELD_NAME

            val extraParam = constructor.parameters.firstOrNull { it.name !in expectedParams && !it.isOptional }

            if (extraParam != null) {
                context.error(
                    "Search parameters class must not have any extra non-optional parameters.",
                    "Expected only these parameters to not be optional: ${expectedParams.sorted()}",
                    IntrospectionLocation.of(paramsType, constructor, extraParam)
                )
            }

            model
        }
    }

    override fun read(input: QueryParams): Params {
        val args = parameterMap.mapValues {
            it.value.read(input)
        }
        return this.constructor.callBy(args)
    }

    private val meta = ListParamsMeta(
        search = search?.describe(),
        filter = filter?.describe(),
        sort = sort?.describe(),
        subset = subset?.describe()
    )

    override fun describe(): ListParamsMeta = meta

    companion object {
        @Throws(UAPITypeError::class)
        fun <P : ListParams> create(
            paramsType: KClass<P>,
            search: SearchParamsReader<*>?,
            filter: FilterParamsReader<*>?,
            sort: SortParamsReader<*>?,
            subset: SubsetParamsReader?,
            constructor: KFunction<P>
        ): DefaultListParamReader<P> {
            val map = mutableMapOf<KParameter, QueryParamReader<*, *>>()

            search?.also {
                val searchParam = constructor.expectParam<SearchParams<*>>(ListParams.WithSearch.FIELD_NAME)
                map[searchParam] = it
            }
            filter?.also {
                val filterParam = constructor.expectParam<Any>(ListParams.WithFilters.FIELD_NAME)
                map[filterParam] = it
            }
            sort?.also {
                val param = constructor.expectParam<SortParams<*>>(ListParams.WithSort.FIELD_NAME)
                map[param] = it
            }
            subset?.also {
                val param = constructor.expectParam<SubsetParams>(ListParams.WithSubset.FIELD_NAME)
                map[param] = it
            }

            return DefaultListParamReader(
                paramsType = paramsType,
                search = search,
                filter = filter,
                sort = sort,
                subset = subset,
                constructor = constructor,
                parameterMap = map
            )
        }
    }

}

private inline fun <reified T : Any> KFunction<*>.expectParam(name: String): KParameter {
    val found =
        this.parameters.find { it.name == name && it.type.withNullability(false).isSubtypeOf(T::class.starProjectedType) }

    return found
        ?: throw UAPITypeError.create(
            this.returnType,
            "Unable to find '$name' parameter of type '${T::class}' in constructor. The compiler should have caught this error. Try recompiling!"
        )
}

