package edu.byu.uapi.server.spi.reflective

import edu.byu.uapi.server.inputs.create
import edu.byu.uapi.server.spi.requireScalarType
import edu.byu.uapi.spi.UAPITypeError
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.input.IdParamMeta
import edu.byu.uapi.spi.input.IdParamReader
import edu.byu.uapi.spi.input.ParamReadFailure
import edu.byu.uapi.spi.requests.IdParams
import edu.byu.uapi.spi.scalars.ScalarType
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class ReflectiveIdParamReader<Id : Any> private constructor(
    private val idType: KClass<Id>,
    private val analyzed: AnalyzedIdParams<Id>
) : IdParamReader<Id> {

    override fun read(input: IdParams): Id {
        val params = analyzed.params.map {
            val value = input[it.name]?.asScalar(it.scalarType)
                ?: throw ParamReadFailure(it.name, idType, "Missing parameter value")
            it.param to value
        }.toMap()
        return analyzed.constructor.callBy(params)
    }

    override fun describe(): IdParamMeta = analyzed

    companion object {
        @Throws(UAPITypeError::class)
        fun <Id: Any> create(
            paramPrefix: String,
            idType: KClass<Id>,
            typeDictionary: TypeDictionary
        ): ReflectiveIdParamReader<Id> {
            val analyzed = analyzeIdParams(idType, typeDictionary, paramPrefix)
            return ReflectiveIdParamReader(idType, analyzed)
        }
    }
}

@Throws(UAPITypeError::class)
internal fun <Id : Any> analyzeIdParams(
    idType: KClass<Id>,
    typeDictionary: TypeDictionary,
    paramPrefix: String
): AnalyzedIdParams<Id> {
    if (!idType.isData) {
        throw UAPITypeError.create(idType, "Complex ID type must be a data class")
    }
    val ctor = idType.primaryConstructor ?: throw UAPITypeError.create(idType, "Unable to find primary constructor.")
    val params: List<AnalyzedIdParam> = ctor.parameters.map { p ->
        val name = p.name ?: throw UAPITypeError.create(idType, "Constructor must have parameter names")
        val classifier = p.type.classifier ?: throw UAPITypeError.create(p.type, "Type must be representable in Kotlin")
        if (classifier !is KClass<*>) {
            throw UAPITypeError.create(classifier, "Type must be a concrete type")
        }
        val scalarType = typeDictionary.requireScalarType(classifier)

        AnalyzedIdParam(paramPrefix + name, p, scalarType)
    }
    return AnalyzedIdParams(
        ctor,
        params
    )
}

internal data class AnalyzedIdParams<Id : Any>(
    val constructor: KFunction<Id>,
    val params: List<AnalyzedIdParam>
) : IdParamMeta {
    override val idParams: List<IdParamMeta.Param> = params.map { IdParamMeta.Param(it.name, it.scalarType.scalarFormat) }
}

internal data class AnalyzedIdParam(
    val name: String,
    val param: KParameter,
    val scalarType: ScalarType<*>
)

