package edu.byu.uapi.spi.introspection

import edu.byu.uapi.spi.dictionary.TypeDictionary
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

interface IntrospectionContext {
    val types: TypeDictionary
    val location: IntrospectionLocation

    val messages: List<IntrospectionMessage>

    fun suggest(
        message: String,
        suggestions: List<String> = emptyList()
    )

    fun suggest(
        message: String,
        suggestion: String
    ) {
        suggest(message, listOf(suggestion))
    }

    fun suggest(
        message: String,
        suggestion: String,
        vararg otherSuggestions: String
    ) {
        suggest(message, listOf(suggestion) + otherSuggestions)
    }

    fun warn(
        message: String,
        suggestions: List<String> = emptyList()
    )

    fun warn(
        message: String,
        suggestion: String
    ) {
        warn(message, listOf(suggestion))
    }

    fun warn(
        message: String,
        suggestion: String,
        vararg otherSuggestions: String
    ) {
        warn(message, listOf(suggestion) + otherSuggestions)
    }

    fun error(
        message: String,
        suggestions: List<String> = emptyList()
    ): Nothing

    fun error(
        message: String,
        suggestion: String
    ): Nothing = error(message, listOf(suggestion))

    fun error(
        message: String,
        suggestion: String,
        vararg otherSuggestions: String
    ): Nothing = error(message, listOf(suggestion) + otherSuggestions)

    fun <R> introspect(target: Introspectable<R>): R

    fun <R> withLocation(location: IntrospectionLocation, fn: IntrospectionContext.() -> R): R
}

fun <R> IntrospectionContext.withLocation(location: KClass<*>, fn: IntrospectionContext.() -> R): R {
    return this.withLocation(IntrospectionLocation.of(location), fn)
}

fun <R> IntrospectionContext.withLocation(
    location: KClass<*>,
    method: KCallable<*>,
    fn: IntrospectionContext.() -> R
): R {
    return this.withLocation(IntrospectionLocation.of(location, method), fn)
}

fun <R> IntrospectionContext.withLocation(
    location: KClass<*>,
    method: KCallable<*>,
    param: KParameter,
    fn: IntrospectionContext.() -> R
): R {
    return this.withLocation(IntrospectionLocation.of(location, method, param), fn)
}

