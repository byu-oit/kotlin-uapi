package edu.byu.uapi.server.spi.reflective

import edu.byu.uapi.server.inputs.typeFailure
import edu.byu.uapi.server.spi.scalarTypeOrFailure
import edu.byu.uapi.spi.dictionary.MaybeTypeFailure
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.functional.Failure
import edu.byu.uapi.spi.functional.Success
import edu.byu.uapi.spi.functional.useFailure
import edu.byu.uapi.spi.input.IdParamMeta
import edu.byu.uapi.spi.input.IdParamReader
import edu.byu.uapi.spi.input.ParamReadFailure
import edu.byu.uapi.spi.input.ParamReadResult
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

//    private val analyzed = analyzeIdParams(idType, typeDictionary, paramPrefix)
//        .onFailure { throw it.asError() }

    override fun read(input: IdParams): ParamReadResult<Id> {
        val params = analyzed.params.map {
            val value = input[it.name]?.asScalar(it.scalarType)
                ?: return Failure(ParamReadFailure(it.name, idType, "Missing parameter value"))
            it.param to value
        }.toMap()
        return Success(analyzed.constructor.callBy(params))
    }

    override fun describe(): IdParamMeta = analyzed

    companion object {
        fun <Id: Any> create(
            paramPrefix: String,
            idType: KClass<Id>,
            typeDictionary: TypeDictionary
        ): MaybeTypeFailure<ReflectiveIdParamReader<Id>> {
            val analyzed = analyzeIdParams(idType, typeDictionary, paramPrefix).useFailure { return it }
            return Success(ReflectiveIdParamReader(idType, analyzed))
        }
    }
}

internal fun <Id : Any> analyzeIdParams(
    idType: KClass<Id>,
    typeDictionary: TypeDictionary,
    paramPrefix: String
): MaybeTypeFailure<AnalyzedIdParams<Id>> {
    if (!idType.isData) {
        return typeFailure(idType, "Complex ID type must be a data class")
    }
    val ctor = idType.primaryConstructor ?: return typeFailure(idType, "Unable to find primary constructor.")
    val params: List<AnalyzedIdParam> = ctor.parameters.map { p ->
        val name = p.name ?: return typeFailure(idType, "Constructor must have parameter names")
        val classifier = p.type.classifier ?: return typeFailure(p.type, "Type must be representable in Kotlin")
        if (classifier !is KClass<*>) {
            return typeFailure(classifier, "Type must be a concrete type")
        }
        val scalarType = typeDictionary.scalarTypeOrFailure(classifier).useFailure { return it }

        AnalyzedIdParam(paramPrefix + name, p, scalarType)
    }
    return Success(AnalyzedIdParams(
        ctor,
        params
    ))
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

