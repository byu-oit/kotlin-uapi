package edu.byu.uapidsl.converters

import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.net.URL
import java.time.Instant
import java.util.*
import kotlin.reflect.KClass


class ScalarConverters {

}


interface ScalarInConverter<Type : Any> {
    fun convertIn(incoming: String, actualType: KClass<in Type>): Type
}

interface ScalarOutConverter<Type : Any> {
    fun convertOut(outgoing: Type): String
}

interface BidirectionalScalarConverter<Type : Any> : ScalarInConverter<Type>, ScalarOutConverter<Type>

//typealias ScalarInConverter<Type> = (input: String) -> Type
//typealias ScalarOutConverter<Type> = (value: Type) -> String

class SimpleConverter<Type : Any>(
    val inConverter: (String) -> Type
) : BidirectionalScalarConverter<Type> {
    override fun convertIn(incoming: String, actualType: KClass<in Type>): Type = inConverter(incoming)

    override fun convertOut(outgoing: Type): String = outgoing.toString()
}

class BasicEnumConverter : BidirectionalScalarConverter<Enum<*>> {

    override fun convertIn(incoming: String, actualType: KClass<in Enum<*>>): Enum<*> {
//        actualType.java.enumConstants.filter {  }
//        enumValueOf<KClass>()
        TODO("not implemented")
    }

    override fun convertOut(outgoing: Enum<*>): String = outgoing.name

}

private fun <Type : Any> s(func: (String) -> Type) = SimpleConverter(func)

val defaultConverters = mapOf<KClass<*>, BidirectionalScalarConverter<*>>(
    String::class to s(::identity),
    Byte::class to s(String::toByte),
    Short::class to s(String::toShort),
    Int::class to s(String::toInt),
    Long::class to s(String::toLong),
    BigInteger::class to s(String::toBigInteger),
    BigDecimal::class to s(String::toBigDecimal),
    URI::class to s({ it: String -> URI(it) }),
    URL::class to s({ it: String -> URL(it) }),
    UUID::class to s(UUID::fromString),
    Instant::class to s({ str: String -> Instant.parse(str) })
)

private fun <Type> identity(obj: Type): Type = obj

