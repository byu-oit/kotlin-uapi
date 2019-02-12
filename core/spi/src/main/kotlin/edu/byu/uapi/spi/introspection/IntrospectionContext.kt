package edu.byu.uapi.spi.introspection

import edu.byu.uapi.spi.dictionary.TypeDictionary
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

interface IntrospectionContext {
    val types: TypeDictionary
    val location: IntrospectionLocation

    val schemaGenerator: SchemaGenerator

    val messages: List<IntrospectionMessage>

    fun suggest(
        message: String,
        suggestions: List<String> = emptyList(),
        location: IntrospectionLocation = this.location
    )

    fun suggest(
        message: String,
        suggestion: String,
        location: IntrospectionLocation = this.location
    ) {
        suggest(message, listOf(suggestion), location)
    }

    fun suggest(
        message: String,
        suggestion: String,
        vararg otherSuggestions: String,
        location: IntrospectionLocation = this.location
    ) {
        suggest(message, listOf(suggestion) + otherSuggestions, location)
    }

    fun warn(
        message: String,
        suggestions: List<String> = emptyList(),
        location: IntrospectionLocation = this.location
    )

    fun warn(
        message: String,
        suggestion: String,
        location: IntrospectionLocation = this.location
    ) {
        warn(message, listOf(suggestion), location)
    }

    fun warn(
        message: String,
        suggestion: String,
        vararg otherSuggestions: String,
        location: IntrospectionLocation = this.location
    ) {
        warn(message, listOf(suggestion) + otherSuggestions, location)
    }

    fun error(
        message: String,
        suggestions: List<String> = emptyList(),
        location: IntrospectionLocation = this.location
    ): Nothing

    fun error(
        message: String,
        suggestion: String,
        location: IntrospectionLocation = this.location
    ): Nothing = error(message, listOf(suggestion), location)

    fun error(
        message: String,
        suggestion: String,
        vararg otherSuggestions: String,
        location: IntrospectionLocation = this.location
    ): Nothing = error(message, listOf(suggestion) + otherSuggestions, location)

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

