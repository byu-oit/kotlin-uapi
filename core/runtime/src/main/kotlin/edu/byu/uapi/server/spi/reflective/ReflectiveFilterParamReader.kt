package edu.byu.uapi.server.spi.reflective

import edu.byu.uapi.server.inputs.typeFailure
import edu.byu.uapi.server.spi.scalarTypeOrFailure
import edu.byu.uapi.spi.dictionary.MaybeTypeFailure
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
        val values = analyzed.fields.map { it.parameter to it.read(input) }.toMap()
        return Success(analyzed.constructor.callBy(values))
    }

    override fun describe(): FilterParamsMeta = meta

    companion object {
        fun <Filters: Any> create(
            filterType: KClass<Filters>,
            typeDictionary: TypeDictionary
        ): MaybeTypeFailure<ReflectiveFilterParamReader<Filters>> {
            val analyzed = analyzeFilterParams(filterType, typeDictionary)
                .useFailure { return it }
            return Success(ReflectiveFilterParamReader(analyzed))
        }
    }
}

internal fun <T : Any> analyzeFilterParams(
    toAnalyze: KClass<T>,
    dictionary: TypeDictionary
): MaybeTypeFailure<AnalyzedFilterParams<T>> {
    if (!toAnalyze.isData) {
        return typeFailure(toAnalyze, "Filter type must be a data class")
    }
    val ctor = toAnalyze.primaryConstructor
        ?: return typeFailure(toAnalyze, "Missing primary constructor. As this is a data class, this shouldn't be possible?!")
    val params = ctor.parameters
    val analyzedParams = params.map { p ->
        analyzeFilterParam(p, dictionary)
            .useFailure { return it }
    }

    return Success(AnalyzedFilterParams(
        toAnalyze,
        analyzedParams,
        ctor
    ))
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

    override fun read(queryParams: QueryParams): ParamReadResult<Any?> = queryParams[name]?.asScalarList(scalarType) ?: Success(null)
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
        val values = this.fields.map { it.parameter to it.read(nested) }.toMap()
        return Success(constructor.callBy(values))
    }
}

private val collectionStar = Collection::class.starProjectedType

internal fun analyzeFilterParam(
    param: KParameter,
    dictionary: TypeDictionary
): MaybeTypeFailure<AnalyzedFilterField> {
    if (param.type.withNullability(false).isSubtypeOf(collectionStar)) {
        return analyzeCollectionFilter(param, dictionary)
    }
    val classifier = param.type.classifier ?: return typeFailure(param.type, "Type must be representable in Kotlin")
    if (classifier is KClass<*> && dictionary.isScalarType(classifier)) {
        return analyzeValueParam(param, classifier, dictionary)
    }
    return analyzeComplexFilter(param, dictionary)
}

internal fun analyzeComplexFilter(
    param: KParameter,
    dictionary: TypeDictionary
): MaybeTypeFailure<AnalyzedComplexFilterField> {
    val type = param.type.classifier!!

    if (type !is KClass<*>) {
        return typeFailure(type, "Expected a concrete type for ${param.name}")
    }
    if (!type.isData) {
        return typeFailure(type, "Expected a data class for ${param.name}")
    }
    val ctor = type.primaryConstructor ?: return typeFailure(type, "Expected a primary constructor")
    val params = ctor.parameters.map { p ->
        analyzeFilterParam(p, dictionary).useFailure { return it }
    }
    return Success(AnalyzedComplexFilterField(
        param,
        param.name!!,
        type,
        params,
        ctor
    ))
}

internal fun analyzeValueParam(
    param: KParameter,
    type: KClass<*>,
    dictionary: TypeDictionary
): MaybeTypeFailure<AnalyzedSimpleFilterField> {
    val required = !param.type.isMarkedNullable && !param.isOptional
    if (required) {
        return typeFailure(param.type, "Parameter ${param.name} must be nullable or have a default value")
    }
    return Success(AnalyzedSimpleFilterField(
        param,
        param.name!!,
        type,
        dictionary.scalarTypeOrFailure(type).useFailure { return it }
    ))
}

private fun analyzeCollectionFilter(
    parameter: KParameter,
    dictionary: TypeDictionary
): MaybeTypeFailure<AnalyzedFilterField> {
    val collectionType = parameter.type.classifier!!

    val collectionCreator: ContainerCreator<*, *> = when (collectionType) {
        List::class -> { it -> it.toList() }
        Set::class -> { it -> it.toSet() } //TODO(someday) optimize this for Enums using EnumSet
        Collection::class -> { it -> it.toSet() }
        else -> {
            return typeFailure(parameter.type, "Invalid collection type for ${parameter.name}. Must be a List, Set, or Collection")
        }
    }

    val itemProjection = parameter.type.arguments.first()
    val itemType = itemProjection.type
        ?: return typeFailure(parameter.type, "Type for ${parameter.name} must not be a '*' projection")
    val item = itemType.classifier!!

    if (item !is KClass<*>) {
        return typeFailure(item, "Expected a concrete type for ${parameter.name}")
    }
    val scalar = dictionary.scalarTypeOrFailure(item).useFailure { return it }
    return Success(AnalyzedRepeatableFilterField(
        parameter,
        parameter.name!!,
        item,
        collectionCreator,
        scalar
    ))
}



