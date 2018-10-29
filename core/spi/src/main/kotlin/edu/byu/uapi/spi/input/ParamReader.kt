package edu.byu.uapi.spi.input

import edu.byu.uapi.spi.functional.SuccessOrFailure
import edu.byu.uapi.spi.scalars.ScalarFormat
import kotlin.reflect.KClass

interface ParamReader<ParamType : Any, InputType : Any, MetaType : Any> {
    fun read(input: InputType): ParamReadResult<ParamType>
    fun describe(): MetaType
}

typealias QueryParams = Map<String, Set<String>>

interface QueryParamReader<T : Any, Meta : QueryParamMetadata> : ParamReader<T, QueryParams, Meta>

interface QueryParamMetadata {
    val queryParams: List<Param>

    data class Param(
        val name: String,
        val format: ScalarFormat,
        val repeatable: Boolean = false
    )
}

typealias ParamReadResult<Type> = SuccessOrFailure<Type, ParamReadFailure>

data class ParamReadFailure(
    val type: KClass<*>,
    val message: String,
    val cause: Throwable? = null
)

class ParamReadException(
    val type: KClass<*>,
    message: String,
    cause: Throwable? = null
) : Exception(
    "Error reading parameter of type $type: $message",
    cause
)

fun ParamReadFailure.thrown(): Nothing {
    throw ParamReadException(this.type, this.message, this.cause)
}
