package edu.byu.uapidsl.typemodeling

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import kotlin.reflect.KClass

interface TypeModeler {

    fun jsonMapper(): ObjectMapper

    @Throws(UnmappableTypeException::class)
    fun jsonReaderFor(type: KClass<*>): ObjectReader

    @Throws(UnmappableTypeException::class)
    fun getJsonWriterFor(type: KClass<*>): ObjectWriter

    @Throws(UnmappableTypeException::class)
    fun validateForJsonOutput(type: KClass<*>)

    @Throws(UnmappableTypeException::class)
    fun validateForPathParamInput(type: KClass<*>)

    @Throws(UnmappableTypeException::class)
    fun validateForQueryParamInput(type: KClass<*>)

    @Throws(UnmappableTypeException::class)
    fun <Type : Any> queryParamReaderFor(type: KClass<Type>): QueryParamReader<Type>

    @Throws(UnmappableTypeException::class)
    fun queryParamSchemaFor(type: KClass<*>): QueryParamSchema

    @Throws(UnmappableTypeException::class)
    fun <Type : Any> pathParamReaderFor(type: KClass<Type>): PathParamReader<Type>

    @Throws(UnmappableTypeException::class)
    fun pathParamSchemaFor(type: KClass<*>): PathParamSchema<*>

    @Throws(UnmappableTypeException::class)
    fun outputSchemaFor(type: KClass<*>): OutputSchema

    @Throws(UnmappableTypeException::class)
    fun jsonSchemaFor(type: KClass<*>): JsonSchema

    @Throws(UnmappableTypeException::class)
    fun jsonInputSchemaFor(type: KClass<*>): JsonInputSchema

    fun genericJsonWriter(): ObjectWriter
}

class UnmappableTypeException(
    message: String
) : RuntimeException(message)

