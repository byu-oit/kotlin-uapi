package edu.byu.uapi.server.spi

import edu.byu.uapi.server.inputs.typeError
import edu.byu.uapi.spi.dictionary.MaybeTypeFailure
import edu.byu.uapi.spi.functional.Failure
import edu.byu.uapi.spi.functional.Success
import edu.byu.uapi.spi.functional.useFailure
import edu.byu.uapi.spi.input.*
import edu.byu.uapi.spi.requests.QueryParams
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class DefaultListParamReader<Params : ListParams> private constructor(
    private val search: SearchParamsReader<*>?,
    private val filter: FilterParamsReader<*>?,
    private val sort: SortParamsReader<*>?,
    private val subset: SubsetParamsReader?,
    private val constructor: KFunction<Params>,
    private val parameterMap: Map<KParameter, QueryParamReader<*, *>>
) : ListParamReader<Params> {
    override fun read(input: QueryParams): ParamReadResult<Params> {
        val args = parameterMap.mapValues {
            it.value.read(input).useFailure { f -> return f }
        }
        return Success(this.constructor.callBy(args))
    }

    private val meta = ListParamsMeta(
        search = search?.describe(),
        filter = filter?.describe(),
        sort = sort?.describe(),
        subset = subset?.describe()
    )

    override fun describe(): ListParamsMeta = meta

    companion object {
        fun <P : ListParams> create(
            search: SearchParamsReader<*>?,
            filter: FilterParamsReader<*>?,
            sort: SortParamsReader<*>?,
            subset: SubsetParamsReader?,
            constructor: KFunction<P>
        ): MaybeTypeFailure<DefaultListParamReader<P>> {
            val map = mutableMapOf<KParameter, QueryParamReader<*, *>>()

            try {
                search?.also {
                    val searchParam = constructor.expectParam<SearchParams<*>>(ListParams.Searching.FIELD_NAME)
                    map[searchParam] = it
                }
                filter?.also {
                    val filterParam = constructor.expectParam<Any>(ListParams.Filtering.FIELD_NAME)
                    map[filterParam] = it
                }
                sort?.also {
                    val param = constructor.expectParam<SortParams<*>>(ListParams.Sorting.FIELD_NAME)
                    map[param] = it
                }
                subset?.also {
                    val param = constructor.expectParam<SubsetParams>(ListParams.SubSetting.FIELD_NAME)
                    map[param] = it
                }
            } catch (ex: UAPITypeError) {
                return Failure(ex.toFailure())
            }

            return Success(DefaultListParamReader(
                search = search,
                filter = filter,
                sort = sort,
                subset = subset,
                constructor = constructor,
                parameterMap = map
            ))
        }
    }

}

private inline fun <reified T : Any> KFunction<*>.expectParam(name: String): KParameter {
    val found = this.parameters.find { it.name == name && it.type.isSubtypeOf(T::class.starProjectedType) }

    return found
        ?: typeError(this.returnType, "Unable to find '$name' parameter of type '${T::class}' in constructor. The compiler should have caught this error. Try recompiling!")
}

