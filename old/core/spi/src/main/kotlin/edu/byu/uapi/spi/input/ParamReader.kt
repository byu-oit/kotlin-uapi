package edu.byu.uapi.spi.input

import edu.byu.uapi.model.UAPIValueConstraints
import edu.byu.uapi.model.UAPIValueType
import edu.byu.uapi.spi.UAPITypeError
import edu.byu.uapi.spi.requests.IdParams
import edu.byu.uapi.spi.requests.QueryParams
import kotlin.reflect.KClass

interface ParamReader<ParamType, InputType : Any, MetaType : Any> {
    @Throws(ParamReadFailure::class)
    fun read(input: InputType): ParamType
    @Throws(UAPITypeError::class)
    fun describe(): MetaType
}

interface QueryParamReader<T, Meta : QueryParamMetadata> : ParamReader<T, QueryParams, Meta>

interface QueryParamMetadata {
    val queryParams: List<Param>

    data class Param(
        val name: String,
        val type: UAPIValueType,
        val constraints: UAPIValueConstraints? = null,
        val repeatable: Boolean = false
    )
}

interface IdParamReader<T : Any> : ParamReader<T, IdParams, IdParamMeta>

interface IdParamMeta {
    val idParams: List<Param>

    data class Param(
        val name: String,
        val type: UAPIValueType,
        val constraints: UAPIValueConstraints? = null
    )

    data class Default(override val idParams: List<Param>): IdParamMeta
}

class ParamReadFailure(
    val name: String,
    val type: KClass<*>,
    message: String,
    cause: Throwable? = null
) : Exception(
    "Error reading parameter $name of type $type: $message",
    cause
)
