package edu.byu.uapi.spi.input

import edu.byu.uapi.spi.functional.SuccessOrFailure
import edu.byu.uapi.spi.requests.IdParams
import edu.byu.uapi.spi.requests.QueryParams
import edu.byu.uapi.spi.scalars.ScalarFormat
import kotlin.reflect.KClass

interface ParamReader<ParamType, InputType : Any, MetaType : Any> {
    fun read(input: InputType): ParamReadResult<ParamType>
    fun describe(): MetaType
}

interface QueryParamReader<T, Meta : QueryParamMetadata> : ParamReader<T, QueryParams, Meta>

interface QueryParamMetadata {
    val queryParams: List<Param>

    data class Param(
        val name: String,
        val format: ScalarFormat,
        val repeatable: Boolean = false
    )
}

interface IdParamReader<T : Any> : ParamReader<T, IdParams, IdParamMeta>

interface IdParamMeta {
    val idParams: List<Param>

    data class Param(
        val name: String,
        val format: ScalarFormat
    )

    data class Default(override val idParams: List<Param>): IdParamMeta
}

typealias ParamReadResult<Type> = SuccessOrFailure<Type, ParamReadFailure>

data class ParamReadFailure(
    val name: String,
    val type: KClass<*>,
    val message: String,
    val cause: Throwable? = null
)

class ParamReadException(
    val name: String,
    val type: KClass<*>,
    message: String,
    cause: Throwable? = null
) : Exception(
    "Error reading parameter $name of type $type: $message",
    cause
)

@Suppress("NOTHING_TO_INLINE")
inline fun ParamReadFailure.thrown(): Nothing {
    throw ParamReadException(this.name, this.type, this.message, this.cause)
}
