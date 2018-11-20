package edu.byu.uapi.spi.requests

import edu.byu.uapi.spi.UAPITypeError
import edu.byu.uapi.spi.input.ParamReadFailure
import edu.byu.uapi.spi.scalars.ScalarType
import kotlin.reflect.KClass

typealias QueryParams = Map<String, QueryParam>
typealias IdParams = Map<String, IdParam>

interface Param {
    val name: String
}

interface RequestBody {
    fun <T: Any> readAs(type: KClass<T>): T
}

interface ScalarParam : Param {
    @Throws(ParamReadFailure::class)
    fun asString(): String

    @Throws(ParamReadFailure::class)
    fun <T : Any> asScalar(scalar: ScalarType<T>): T
}

fun ScalarParam.asInt(): Int = this.asString().toIntOrNull()
    ?: throw ParamReadFailure(this.name, Int::class, "Invalid numeric value")

interface WithMultipleValues : Param {
    @Throws(ParamReadFailure::class)
    fun asStringList(): List<String>

    @Throws(ParamReadFailure::class)
    fun <T : Any> asScalarList(scalar: ScalarType<T>): List<T>
}

@Throws(ParamReadFailure::class)
fun WithMultipleValues.asIntList(): List<Int> {
    return this.asStringList().map {
        it.toIntOrNull() ?: throw ParamReadFailure(this.name, Int::class, "Invalid numeric value")
    }
}

interface QueryParam : ScalarParam,
                       WithMultipleValues

interface IdParam : ScalarParam

class StringIdParam(
    override val name: String,
    val value: String
) : IdParam {
    override fun asString(): String = value

    override fun <T : Any> asScalar(scalar: ScalarType<T>): T {
//        return scalar.fromString(value).mapFailure { ParamReadFailure(this.name, scalar.type, it.message) }
        return try {
            scalar.fromString(value)
        } catch (ex: UAPITypeError) {
            throw ParamReadFailure(this.name, scalar.type, ex.typeFailure, ex)
        }
    }
}

class StringSetQueryParam(
    override val name: String,
    val values: Set<String>
) : QueryParam {
    override fun asString(): String {
        if (values.size != 1) {
            throw ParamReadFailure(name, String::class, "Expected exactly one value")
        }
        return values.first()
    }

    override fun <T : Any> asScalar(scalar: ScalarType<T>): T {
        val string = asString()
        return try {
            scalar.fromString(string)
        } catch (ex: UAPITypeError) {
            throw ParamReadFailure(this.name, scalar.type, ex.typeFailure, ex)
        }
    }

    override fun asStringList(): List<String> {
        return values.flatMap { it.split(",") }
    }

    override fun <T : Any> asScalarList(scalar: ScalarType<T>): List<T> {
        return asStringList().map {
            try {
                scalar.fromString(it)
            } catch (ex: UAPITypeError) {
                throw ParamReadFailure(this.name, scalar.type, ex.typeFailure, ex)
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
