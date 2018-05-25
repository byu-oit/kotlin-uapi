package edu.byu.uapidsl.typemodeling

import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import kotlin.reflect.KClass

class CachingTypeModelerDecorator(private val delegate: TypeModeler) : TypeModeler by delegate {

    private val jsonReaderCache: LoadingCache<KClass<*>, ObjectReader> = Caffeine.newBuilder()
        .softValues()
        .build(delegate::getJsonReaderFor)

    private val jsonWriterCache: LoadingCache<KClass<*>, ObjectWriter> = Caffeine.newBuilder()
        .softValues()
        .build(delegate::getJsonWriterFor)

    private val schemaCache: LoadingCache<KClass<*>, JsonSchema> = Caffeine.newBuilder()
        .softValues()
        .build(delegate::jsonSchemaFor)

    private val queryParamSchemaCache = Caffeine.newBuilder()
        .softValues()
        .build(delegate::queryParamSchemaFor)

    private val pathParamSchemaCache = Caffeine.newBuilder()
        .softValues()
        .build(delegate::pathParamSchemaFor)

    private val outputSchemaCache = Caffeine.newBuilder()
        .softValues()
        .build(delegate::outputSchemaFor)

    override fun getJsonReaderFor(type: KClass<*>): ObjectReader = jsonReaderCache[type]!!

    override fun getJsonWriterFor(type: KClass<*>): ObjectWriter = jsonWriterCache[type]!!

    override fun queryParamSchemaFor(type: KClass<*>): QueryParamSchema = queryParamSchemaCache[type]!!

    override fun pathParamSchemaFor(type: KClass<*>): PathParamSchema<*> = pathParamSchemaCache[type]!!

    override fun outputSchemaFor(type: KClass<*>): OutputSchema = outputSchemaCache[type]!!

    override fun jsonSchemaFor(type: KClass<*>): JsonSchema = schemaCache[type]!!
}
