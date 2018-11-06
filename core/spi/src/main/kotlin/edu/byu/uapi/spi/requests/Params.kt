package edu.byu.uapi.spi.requests

import edu.byu.uapi.spi.UAPITypeError
import edu.byu.uapi.spi.functional.*
import edu.byu.uapi.spi.input.ParamReadFailure
import edu.byu.uapi.spi.input.ParamReadResult
import edu.byu.uapi.spi.scalars.ScalarType

typealias QueryParams = Map<String, QueryParam>
typealias IdParams = Map<String, IdParam>

interface Param {
    val name: String
}

interface ScalarParam : Param {
    fun asString(): ParamReadResult<String>
    fun <T : Any> asScalar(scalar: ScalarType<T>): ParamReadResult<T>
}

fun ScalarParam.asInt(): ParamReadResult<Int> = this.asString().flatMap {
    try {
        Success(it.toInt())
    } catch (ex: NumberFormatException) {
        Failure(ParamReadFailure(this.name, Int::class, "Invalid numeric value", ex))
    }
}

interface WithMultipleValues : Param {
    fun asStringList(): ParamReadResult<List<String>>
    fun <T : Any> asScalarList(scalar: ScalarType<T>): ParamReadResult<List<T>>
}

fun WithMultipleValues.asIntList(): ParamReadResult<List<Int>> {
    return this.asStringList().map { strings ->
        strings.map {
            try {
                it.toInt()
            } catch (ex: NumberFormatException) {
                return Failure(ParamReadFailure(this.name, Int::class, "Invalid numeric value", ex))
            }
        }
    }
}

interface QueryParam : ScalarParam,
                       WithMultipleValues

interface IdParam : ScalarParam

class StringIdParam(
    override val name: String,
    val value: String
) : IdParam {
    override fun asString(): ParamReadResult<String> = Success(value)

    override fun <T : Any> asScalar(scalar: ScalarType<T>): ParamReadResult<T> {
//        return scalar.fromString(value).mapFailure { ParamReadFailure(this.name, scalar.type, it.message) }
        return try {
            Success(scalar.fromString(value))
        } catch (ex: UAPITypeError) {
            return Failure(ParamReadFailure(this.name, scalar.type, ex.typeFailure, ex))
        }
    }
}

class StringSetQueryParam(
    override val name: String,
    val values: Set<String>
) : QueryParam {
    override fun asString(): ParamReadResult<String> {
        if (values.size != 1) {
            return Failure(ParamReadFailure(name, String::class, "Expected exactly one value"))
        }
        return Success(values.first())
    }

    override fun <T : Any> asScalar(scalar: ScalarType<T>): ParamReadResult<T> {
        val string = asString().useFailure { return it }
        return try {
            Success(scalar.fromString(string))
        } catch (ex: UAPITypeError) {
            return Failure(ParamReadFailure(this.name, scalar.type, ex.typeFailure, ex))
        }
    }

    override fun asStringList(): ParamReadResult<List<String>> {
        return Success(values.flatMap { it.split(",") })
    }

    override fun <T : Any> asScalarList(scalar: ScalarType<T>): ParamReadResult<List<T>> {
        return asStringList().map { values ->
            values.map {
                scalar.fromString(it)
                //TODO: catch type errors and convert to param failures
            }
        }
    }
}

fun QueryParams.withPrefix(prefix: String): QueryParams {
    return this.asSequence()
        .filter { it.key.startsWith(prefix) }
        .map { it.key.substring(prefix.length) to it.value }
        .toMap()
}
