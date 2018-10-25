package edu.byu.uapi.server.spi.reflective

import edu.byu.uapi.server.inputs.typeError
import edu.byu.uapi.server.spi.UAPITypeError
import edu.byu.uapi.server.spi.requireScalarType
import edu.byu.uapi.spi.annotations.DefaultSort
import edu.byu.uapi.spi.annotations.SearchFields
import edu.byu.uapi.spi.dictionary.MaybeTypeFailure
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.dictionary.TypeFailure
import edu.byu.uapi.spi.functional.Failure
import edu.byu.uapi.spi.functional.asSuccess
import edu.byu.uapi.spi.input.CollectionParamsMeta
import edu.byu.uapi.spi.input.CollectionParamsProvider
import edu.byu.uapi.spi.input.Params
import edu.byu.uapi.spi.input.QueryParamReader
import edu.byu.uapi.spi.scalars.ScalarType
import kotlin.reflect.*
import kotlin.reflect.full.*

class ReflectiveCollectionParamsProvider<Params : Any>(
    val paramsType: KClass<Params>,
    val dictionary: TypeDictionary
) : CollectionParamsProvider<Params> {

    override fun getReader(): QueryParamReader<Params> {
        TODO("not implemented")
    }

    override fun getMeta(): CollectionParamsMeta {
        TODO("not implemented")
    }

}


internal fun analyzeAndValidate(
    toAnalyze: KClass<*>,
    dictionary: TypeDictionary
): MaybeTypeFailure<AnalyzedParams> {
    try {
        if (!toAnalyze.isData) {
            throw UAPITypeError(toAnalyze, "Collection parameters must be data classes")
        }
        val search = analyzeSearch(toAnalyze, dictionary)
        val filter = analyzeFilter(toAnalyze, dictionary)
        val sort = analyzeSort(toAnalyze, dictionary)

        val constructor = toAnalyze.primaryConstructor!!

        return AnalyzedParams(
            type = toAnalyze,
            search = search,
            filter = filter,
            sort = sort,
            constructor = constructor
        ).asSuccess()
    } catch (err: UAPITypeError) {
        return Failure(TypeFailure(err.type, err.typeFailure, err))
    }
}

@Suppress("UNCHECKED_CAST")
internal fun analyzeSearch(
    toAnalyze: KClass<*>,
    dictionary: TypeDictionary
): AnalyzedSearchParams? {
    val unwrapped = toAnalyze.unwrapParamType(Params.Searching::class) ?: return null

    val enumInfo = unwrapped.getEnumInfo()

    val fields = enumInfo.mapValues {
        it.value.findInstanceOf<SearchFields>()?.value?.toList()
            ?: typeError(unwrapped, "value ${it.key} must be annotated with @SearchFields()")
    }

    val scalarType = dictionary.requireScalarType(unwrapped)

    return AnalyzedSearchParams(unwrapped as KClass<Enum<*>>, enumInfo.keys.toList(), fields, scalarType)
}

private inline fun <reified T : Any> Iterable<*>.findInstanceOf(): T? {
    return this.find { it is T } as T?
}

@Suppress("UNCHECKED_CAST")
internal fun analyzeSort(
    toAnalyze: KClass<*>,
    dictionary: TypeDictionary
): AnalyzedSortParams? {
    val unwrapped = toAnalyze.unwrapParamType(Params.Searching::class) ?: return null

    val enumInfo = unwrapped.getEnumInfo()

    val defaults = enumInfo.asSequence()
        .map { it.key to it.value.findInstanceOf<DefaultSort>() }
        .filter { it.second != null }
        .sortedBy { it.second?.order }
        .map { it.first }
        .toList()

    val scalarType = dictionary.requireScalarType(unwrapped)

    return AnalyzedSortParams(unwrapped as KClass<Enum<*>>, enumInfo.keys.toList(), defaults, scalarType)
}

@Suppress("UNCHECKED_CAST")
internal fun analyzeFilter(
    toAnalyze: KClass<*>,
    dictionary: TypeDictionary
): AnalyzedFilterParams? {
    val unwrapped = toAnalyze.unwrapParamType(Params.Filtering::class) ?: return null

    if (!unwrapped.isData) {
        typeError(unwrapped, "Type parameter for Params.Filtering must be a data class")
    }

    val ctor = unwrapped.primaryConstructor
        ?: typeError(unwrapped, "Missing primary constructor. As this is a data class, this shouldn't be possible?!")
    val params = ctor.parameters
    val analyzedParams = params.map { analyzeFilterParam(it, dictionary) }

    return AnalyzedFilterParams(
        unwrapped,
        analyzedParams,
        ctor
    )
}

private val collectionStar = Collection::class.starProjectedType

private fun analyzeFilterParam(
    param: KParameter,
    dictionary: TypeDictionary
): AnalyzedFilterField {
    if (param.type.withNullability(false).isSubtypeOf(collectionStar)) {
        return analyzeCollectionFilter(param, dictionary)
    }
    val classifier = param.type.classifier ?: typeError(param.type, "Type must be representable in Kotlin")
    if (classifier is KClass<*> && dictionary.isScalarType(classifier)) {
        return analyzeValueParam(param, classifier, dictionary)
    }
    return analyzeComplexFilter(param, dictionary)
}

internal fun analyzeComplexFilter(
    param: KParameter,
    dictionary: TypeDictionary
): AnalyzedComplexFilterField {
    val type = param.type.classifier!!

    if (type !is KClass<*>) {
        typeError(type, "Expected a concrete type for ${param.name}")
    }
    if (!type.isData) {
        typeError(type, "Expected a data class for ${param.name}")
    }
    val ctor = type.primaryConstructor ?: typeError(type, "Expected a primary constructor")
    val params = ctor.parameters.map { analyzeFilterParam(it, dictionary) }
    return AnalyzedComplexFilterField(
        param,
        param.name!!,
        type,
        params,
        ctor
    )
}

internal fun analyzeValueParam(
    param: KParameter,
    type: KClass<*>,
    dictionary: TypeDictionary
): AnalyzedSimpleFilterField {
    val required = !param.type.isMarkedNullable && !param.isOptional
    if (required) {
        typeError(param.type, "Parameter ${param.name} must be nullable or have a default value")
    }
    return AnalyzedSimpleFilterField(
        param,
        param.name!!,
        type,
        dictionary.requireScalarType(type)
    )
}

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
            typeError(parameter.type, "Invalid collection type for ${parameter.name}. Must be a List, Set, or Collection")
        }
    }

    val itemProjection = parameter.type.arguments.first()
    val itemType = itemProjection.type
        ?: typeError(parameter.type, "Type for ${parameter.name} must not be a '*' projection")
    val item = itemType.classifier!!

    if (item !is KClass<*>) {
        typeError(item, "Expected a concrete type for ${parameter.name}")
    }
    val scalar = dictionary.requireScalarType(item)
    return AnalyzedRepeatableFilterField(
        parameter,
        parameter.name!!,
        item,
        collectionCreator,
        scalar
    )
}

private typealias EnumInfo = Map<Enum<*>, List<Annotation>>

private fun KClass<*>.getEnumInfo(): EnumInfo {
    if (!this.isSubclassOf(Enum::class)) {
        typeError(this, "Expected ${this} to be an Enum")
    }
    @Suppress("UNCHECKED_CAST")
    val constants = this.java.enumConstants as Array<Enum<*>>
    return constants.map {
        val enumField = this.java.getField(it.name)
        it to enumField.annotations.toList()
    }.toMap()
}

private fun KClass<*>.unwrapParamType(wrappingType: KClass<*>): KClass<*>? {
    val foundSuper = this.findSuperTypeFor(wrappingType) ?: return null

    val foundType = foundSuper.arguments.first()

    if (foundType.variance !== KVariance.INVARIANT) {
        typeError(this, "Type parameter for ${wrappingType.simpleName} must not be an 'in' or 'out' type.")
    }
    val actualType = foundType.type
        ?: typeError(this, "Type parameter for ${wrappingType.simpleName} must be a concrete type, not a '*' projection")
    val actual = actualType.classifier
        ?: typeError(this, "Type parameter for ${wrappingType.simpleName} must be a valid Kotlin type")

    if (actual !is KClass<*>) {
        typeError(actual, "Type parameter for ${wrappingType.simpleName} must be a concrete class")
    }

    return actual
}

private fun KClass<*>.findSuperTypeFor(type: KClass<*>): KType? {
    val star = type.starProjectedType
    return this.supertypes.find { it.isSubtypeOf(star) }
}


internal data class AnalyzedParams(
    val type: KClass<*>,
    val search: AnalyzedSearchParams?,
    val filter: AnalyzedFilterParams?,
    val sort: AnalyzedSortParams?,
    val constructor: KFunction<*>
)

internal data class AnalyzedSearchParams(
    val contextClass: KClass<Enum<*>>,
    val contexts: List<Enum<*>>,
    val fields: Map<Enum<*>, List<String>>,
    val scalarType: ScalarType<*>
)

internal data class AnalyzedSortParams(
    val fieldClass: KClass<Enum<*>>,
    val fields: List<Enum<*>>,
    val defaults: List<Enum<*>>,
    val scalarType: ScalarType<*>
)

internal data class AnalyzedFilterParams(
    val filterClass: KClass<*>,
    val fields: List<AnalyzedFilterField>, //In constructor order
    val constructor: KFunction<*>
)

internal sealed class AnalyzedFilterField {
    abstract val parameter: KParameter
    abstract val name: String
    abstract val type: KClass<*>
}

internal data class AnalyzedSimpleFilterField(
    override val parameter: KParameter,
    override val name: String,
    override val type: KClass<*>,
    val scalarType: ScalarType<*>
) : AnalyzedFilterField()

typealias ContainerCreator<Item, Container> = (Iterable<Item>) -> Container

internal data class AnalyzedRepeatableFilterField(
    override val parameter: KParameter,
    override val name: String,
    override val type: KClass<*>,
    val containerCreator: ContainerCreator<*, *>,
    val scalarType: ScalarType<*>
) : AnalyzedFilterField()

internal data class AnalyzedComplexFilterField(
    override val parameter: KParameter,
    override val name: String,
    override val type: KClass<*>,
    val fields: List<AnalyzedFilterField>,
    val constructor: KFunction<*>
) : AnalyzedFilterField() {
}

