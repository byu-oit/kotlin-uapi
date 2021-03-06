package edu.byu.uapi.server.spi.reflective

import edu.byu.uapi.model.UAPIListFilterParameter
import edu.byu.uapi.model.UAPIListFiltersFeature
import edu.byu.uapi.server.inputs.create
import edu.byu.uapi.server.spi.requireScalarType
import edu.byu.uapi.server.util.exhaustive
import edu.byu.uapi.server.util.toSnakeCase
import edu.byu.uapi.spi.UAPITypeError
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.input.FilterParamsMeta
import edu.byu.uapi.spi.input.FilterParamsReader
import edu.byu.uapi.spi.input.QueryParamMetadata
import edu.byu.uapi.spi.introspection.IntrospectionContext
import edu.byu.uapi.spi.introspection.withLocation
import edu.byu.uapi.spi.requests.QueryParams
import edu.byu.uapi.spi.requests.withPrefix
import edu.byu.uapi.spi.scalars.ScalarType
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability

class ReflectiveFilterParamReader<Filters : Any> internal constructor(
    private val analyzed: AnalyzedFilterParams<Filters>
) : FilterParamsReader<Filters> {

    private val meta = analyzed.meta

    override fun read(input: QueryParams): Filters? {
        val hasAny = analyzed.fieldNames.any { input.containsKey(it) }
        if (!hasAny) {
            return null
        }
        return analyzed.constructor.invoke(input, analyzed.fields)
    }

    override fun describe(): FilterParamsMeta = meta

    override fun introspect(context: IntrospectionContext): UAPIListFiltersFeature {
        return context.withLocation(analyzed.filterClass) {
            analyzed.fields.flatMap { af ->
                context.withLocation(analyzed.filterClass, analyzed.constructor, af.parameter) {
                    af.getParams("").map {
                        it.name to UAPIListFilterParameter(
                            type = it.type,
                            constraints = it.constraints,
                            allowMultiple = it.repeatable
                        )
                    }
                }
            }.toMap().toSortedMap()
        }
    }

    companion object {
        @Throws(UAPITypeError::class)
        fun <Filters : Any> create(
            filterType: KClass<Filters>,
            typeDictionary: TypeDictionary
        ): ReflectiveFilterParamReader<Filters> {
            val analyzed = analyzeFilterParams(filterType, typeDictionary)
            return ReflectiveFilterParamReader(analyzed)
        }
    }
}

@Throws(UAPITypeError::class)
internal fun <T : Any> analyzeFilterParams(
    toAnalyze: KClass<T>,
    dictionary: TypeDictionary
): AnalyzedFilterParams<T> {
    if (!toAnalyze.isData) {
        throw UAPITypeError.create(toAnalyze, "Filter type must be a data class")
    }
    val ctor = toAnalyze.primaryConstructor
        ?: throw UAPITypeError.create(
            toAnalyze,
            "Missing primary constructor. As this is a data class, this shouldn't be possible?!"
        )
    val params = ctor.parameters
    val analyzedParams = params.map { p ->
        analyzeFilterParam(p, dictionary)
    }

    return AnalyzedFilterParams(
        toAnalyze,
        analyzedParams,
        ctor
    )
}

internal data class AnalyzedFilterParams<T : Any>(
    val filterClass: KClass<T>,
    val fields: List<AnalyzedFilterField>, //In constructor order
    val constructor: KFunction<T>
) {
    val meta: FilterParamsMeta = FilterParamsMeta(
        fields.flatMap { f -> f.getParams("") }
    )

    val fieldNames = meta.queryParams.map { it.name }
}

internal sealed class AnalyzedFilterField {
    abstract val parameter: KParameter
    abstract val name: String
    abstract val type: KClass<*>

    abstract fun getParams(prefix: String): List<QueryParamMetadata.Param>
    abstract fun read(queryParams: QueryParams): ReadResult

    val hasDefault: Boolean
        get() = parameter.isOptional
}

internal sealed class ReadResult {
    data class Value(val value: Any?) : ReadResult()
    object UseDefault : ReadResult()
    object Missing : ReadResult()
}

internal data class AnalyzedSimpleFilterField(
    override val parameter: KParameter,
    override val name: String,
    override val type: KClass<*>,
    val scalarType: ScalarType<*>
) : AnalyzedFilterField() {
    override fun getParams(prefix: String): List<QueryParamMetadata.Param> = listOf(
        QueryParamMetadata.Param(
            prefix + name, scalarType.valueType, scalarType.constraints, false
        )
    )

    override fun read(queryParams: QueryParams): ReadResult {
        return queryParams[name]?.let {
            ReadResult.Value(it.asScalar(scalarType))
        } ?: if (hasDefault) ReadResult.UseDefault else ReadResult.Missing
    }
}

typealias ContainerCreator<Item, Container> = (Iterable<Item>) -> Container

internal data class AnalyzedRepeatableFilterField(
    override val parameter: KParameter,
    override val name: String,
    override val type: KClass<*>,
    val containerCreator: ContainerCreator<*, *>,
    val scalarType: ScalarType<*>
) : AnalyzedFilterField() {
    override fun getParams(prefix: String): List<QueryParamMetadata.Param> = listOf(
        QueryParamMetadata.Param(
            prefix + name, scalarType.valueType, scalarType.constraints, true
        )
    )

    override fun read(queryParams: QueryParams): ReadResult {
        val found = queryParams[name]
        if (found == null && hasDefault) {
            return ReadResult.UseDefault
        }
        val values = found?.asScalarList(scalarType).orEmpty()
        return ReadResult.Value(containerCreator(values))
    }
}

internal data class AnalyzedComplexFilterField(
    override val parameter: KParameter,
    override val name: String,
    override val type: KClass<*>,
    val fields: List<AnalyzedFilterField>,
    val constructor: KFunction<*>
) : AnalyzedFilterField() {

    private val fieldNames by lazy { fields.map { it.name } }

    override fun getParams(prefix: String): List<QueryParamMetadata.Param> {
        val newPrefix = "$prefix$name."
        return fields.flatMap { it.getParams(newPrefix) }
    }

    override fun read(queryParams: QueryParams): ReadResult {
        val nested = queryParams.withPrefix("$name.")
        val hasAny = this.fieldNames.any { nested.containsKey(it) }
        if (!hasAny) {
            return when {
                hasDefault -> ReadResult.UseDefault
                parameter.type.isMarkedNullable -> ReadResult.Value(null)
                else -> ReadResult.Missing
            }
        }
        return ReadResult.Value(constructor.invoke(queryParams, fields))
    }
}

private fun <T> KFunction<T>.invoke(
    queryParams: QueryParams,
    fields: List<AnalyzedFilterField>
): T {
    val values = mutableMapOf<KParameter, Any?>()
    loop@ for (field in fields) {
        when (val read = field.read(queryParams)) {
            is ReadResult.Value -> values[field.parameter] = read.value
            ReadResult.UseDefault -> continue@loop// do nothing
            ReadResult.Missing -> throw IllegalArgumentException("Missing required parameter '${field.name}'")
        }.exhaustive
    }
    return this.callBy(values)
}

private val collectionStar = Collection::class.starProjectedType

@Throws(UAPITypeError::class)
internal fun analyzeFilterParam(
    param: KParameter,
    dictionary: TypeDictionary
): AnalyzedFilterField {
    if (param.type.withNullability(false).isSubtypeOf(collectionStar)) {
        return analyzeCollectionFilter(param, dictionary)
    }
    val classifier = param.type.classifier
        ?: throw UAPITypeError.create(param.type, "Type must be representable in Kotlin")
    if (classifier is KClass<*> && dictionary.isScalarType(classifier)) {
        return analyzeValueParam(param, classifier, dictionary)
    }
    return analyzeComplexFilter(param, dictionary)
}

@Throws(UAPITypeError::class)
internal fun analyzeComplexFilter(
    param: KParameter,
    dictionary: TypeDictionary
): AnalyzedComplexFilterField {
    val type = param.type.classifier!!

    if (type !is KClass<*>) {
        throw UAPITypeError.create(type, "Expected a concrete type for ${param.name}")
    }
    if (!type.isData) {
        throw UAPITypeError.create(type, "Expected a data class for ${param.name}")
    }
    val ctor = type.primaryConstructor ?: throw UAPITypeError.create(type, "Expected a primary constructor")
    val params = ctor.parameters.map { p ->
        analyzeFilterParam(p, dictionary)
    }
    return AnalyzedComplexFilterField(
        param,
        param.name!!.toSnakeCase(),
        type,
        params,
        ctor
    )
}

@Throws(UAPITypeError::class)
internal fun analyzeValueParam(
    param: KParameter,
    type: KClass<*>,
    dictionary: TypeDictionary
): AnalyzedSimpleFilterField {
    val required = !param.type.isMarkedNullable && !param.isOptional
    if (required) {
        throw UAPITypeError.create(param.type, "Parameter ${param.name} must be nullable or have a default value")
    }
    return AnalyzedSimpleFilterField(
        param,
        param.name!!.toSnakeCase(),
        type,
        dictionary.requireScalarType(type)
    )
}

@Throws(UAPITypeError::class)
private fun analyzeCollectionFilter(
    parameter: KParameter,
    dictionary: TypeDictionary
): AnalyzedFilterField {
    val collectionType = parameter.type.classifier!!

    val collectionCreator: ContainerCreator<*, *> = when (collectionType) {
        List::class -> { it -> it.toList() }
        Set::class -> { it -> it.toSet() } //TODO(someday) optimize this for Enums using EnumSet
        Collection::class -> { it -> it.toSet() }
        else -> {
            throw UAPITypeError.create(
                parameter.type,
                "Invalid collection type for ${parameter.name}. Must be a List, Set, or Collection"
            )
        }
    }

    val itemProjection = parameter.type.arguments.first()
    val itemType = itemProjection.type
        ?: throw UAPITypeError.create(parameter.type, "Type for ${parameter.name} must not be a '*' projection")
    val item = itemType.classifier!!

    if (item !is KClass<*>) {
        throw UAPITypeError.create(item, "Expected a concrete type for ${parameter.name}")
    }
    val scalar = dictionary.requireScalarType(item)
    return AnalyzedRepeatableFilterField(
        parameter,
        parameter.name!!.toSnakeCase(),
        item,
        collectionCreator,
        scalar
    )
}



