package edu.byu.uapi.server.spi.reflective

import edu.byu.uapi.server.inputs.thrown
import edu.byu.uapi.server.spi.requireScalarType
import edu.byu.uapi.server.util.toSnakeCase
import edu.byu.uapi.spi.UAPITypeError
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.functional.Success
import edu.byu.uapi.spi.functional.useFailure
import edu.byu.uapi.spi.input.FilterParamsMeta
import edu.byu.uapi.spi.input.FilterParamsReader
import edu.byu.uapi.spi.input.ParamReadResult
import edu.byu.uapi.spi.input.QueryParamMetadata
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

    private val meta = analyzed.toMeta()

    override fun read(input: QueryParams): ParamReadResult<Filters?> {
        val hasAny = analyzed.fieldNames.any { input.containsKey(it) }
        if (!hasAny) {
            return Success(null)
        }
        val values = analyzed.fields.map { it.parameter to it.read(input).useFailure { f -> return f } }.toMap()
        return Success(analyzed.constructor.callBy(values))
    }

    override fun describe(): FilterParamsMeta = meta

    companion object {
        @Throws(UAPITypeError::class)
        fun <Filters: Any> create(
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
        UAPITypeError.thrown(toAnalyze, "Filter type must be a data class")
    }
    val ctor = toAnalyze.primaryConstructor
        ?: UAPITypeError.thrown(toAnalyze, "Missing primary constructor. As this is a data class, this shouldn't be possible?!")
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
    fun toMeta(): FilterParamsMeta {
        return FilterParamsMeta(
            fields.flatMap { f -> f.getParams("") }
        )
    }

    val fieldNames by lazy { fields.map { it.name } }
}

internal sealed class AnalyzedFilterField {
    abstract val parameter: KParameter
    abstract val name: String
    abstract val type: KClass<*>

    abstract fun getParams(prefix: String): List<QueryParamMetadata.Param>
    abstract fun read(queryParams: QueryParams): ParamReadResult<Any?>
}

internal data class AnalyzedSimpleFilterField(
    override val parameter: KParameter,
    override val name: String,
    override val type: KClass<*>,
    val scalarType: ScalarType<*>
) : AnalyzedFilterField() {
    override fun getParams(prefix: String): List<QueryParamMetadata.Param> = listOf(QueryParamMetadata.Param(
        prefix + name, scalarType.scalarFormat, false
    ))

    override fun read(queryParams: QueryParams): ParamReadResult<Any?> = queryParams[name]?.asScalar(scalarType) ?: Success(null)
}

typealias ContainerCreator<Item, Container> = (Iterable<Item>) -> Container

internal data class AnalyzedRepeatableFilterField(
    override val parameter: KParameter,
    override val name: String,
    override val type: KClass<*>,
    val containerCreator: ContainerCreator<*, *>,
    val scalarType: ScalarType<*>
) : AnalyzedFilterField() {
    override fun getParams(prefix: String): List<QueryParamMetadata.Param> = listOf(QueryParamMetadata.Param(
        prefix + name, scalarType.scalarFormat, true
    ))

    override fun read(queryParams: QueryParams): ParamReadResult<Any?> {
        val values = queryParams[name]?.asScalarList(scalarType)?.useFailure { return it }.orEmpty()
        return Success(containerCreator(values))
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

    override fun read(queryParams: QueryParams): ParamReadResult<Any?> {
        val nested = queryParams.withPrefix("$name.")
        if (nested.isEmpty()) {
            return Success(null)
        }
        val hasAny = this.fieldNames.any { nested.containsKey(it) }
        if (!hasAny) {
            return Success(null)
        }
        val values = this.fields.map { it.parameter to it.read(nested).useFailure { f -> return f } }.toMap()
        return Success(constructor.callBy(values))
    }
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
    val classifier = param.type.classifier ?: UAPITypeError.thrown(param.type, "Type must be representable in Kotlin")
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
        UAPITypeError.thrown(type, "Expected a concrete type for ${param.name}")
    }
    if (!type.isData) {
        UAPITypeError.thrown(type, "Expected a data class for ${param.name}")
    }
    val ctor = type.primaryConstructor ?: UAPITypeError.thrown(type, "Expected a primary constructor")
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
        UAPITypeError.thrown(param.type, "Parameter ${param.name} must be nullable or have a default value")
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
            UAPITypeError.thrown(parameter.type, "Invalid collection type for ${parameter.name}. Must be a List, Set, or Collection")
        }
    }

    val itemProjection = parameter.type.arguments.first()
    val itemType = itemProjection.type
        ?: UAPITypeError.thrown(parameter.type, "Type for ${parameter.name} must not be a '*' projection")
    val item = itemType.classifier!!

    if (item !is KClass<*>) {
        UAPITypeError.thrown(item, "Expected a concrete type for ${parameter.name}")
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



