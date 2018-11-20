package edu.byu.uapi.http.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.gson.Gson
import edu.byu.uapi.http.HttpRequestBody
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.rendering.Renderer
import edu.byu.uapi.spi.requests.RequestBody
import java.io.Writer
import javax.json.spi.JsonProvider
import javax.json.stream.JsonGenerator
import kotlin.reflect.KClass
import com.fasterxml.jackson.core.JsonGenerator as JacksonGenerator
import com.google.gson.JsonObject as GsonObject
import javax.json.JsonObject as JavaxJsonObject

sealed class JsonEngine<Output : Any, Args> {
    abstract fun renderer(
        typeDictionary: TypeDictionary,
        args: Args
    ): Renderer<Output>

    abstract fun resourceBody(
        body: HttpRequestBody,
        typeDictionary: TypeDictionary
    ): RequestBody
}

object JacksonEngine : JsonEngine<JacksonGenerator, Writer>() {

    private val objectMapper = ObjectMapper()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false)
        .registerModules(
            KotlinModule(),
            JavaTimeModule()
        )

    override fun renderer(
        typeDictionary: TypeDictionary,
        args: Writer
    ): Renderer<JacksonGenerator> {
        return JacksonRenderer(typeDictionary, objectMapper.factory.createGenerator(args))
    }

    override fun resourceBody(
        body: HttpRequestBody,
        typeDictionary: TypeDictionary
    ): RequestBody {
        return object : RequestBody {
            override fun <T : Any> readAs(type: KClass<T>): T {
                return objectMapper.readValue(body.asReader(), type.java)
            }
        }
    }
}

object GsonTreeEngine : JsonEngine<GsonObject, Any?>() {
    override fun renderer(
        typeDictionary: TypeDictionary,
        args: Any?
    ): Renderer<GsonObject> {
        return GsonTreeRenderer(typeDictionary, Gson())
    }

    override fun resourceBody(
        body: HttpRequestBody,
        typeDictionary: TypeDictionary
    ): RequestBody {
        TODO("not implemented")
    }
}

internal val jsonProvider: JsonProvider by lazy { JsonProvider.provider() }

object JavaxJsonTreeEngine : JsonEngine<JavaxJsonObject, Any?>() {
    override fun renderer(
        typeDictionary: TypeDictionary,
        args: Any?
    ): Renderer<JavaxJsonObject> {
        return JavaxJsonTreeRenderer(typeDictionary, jsonProvider)
    }

    override fun resourceBody(
        body: HttpRequestBody,
        typeDictionary: TypeDictionary
    ): RequestBody {
        TODO("not implemented")
    }
}

object JavaxJsonStreamEngine : JsonEngine<JsonGenerator, Writer>() {
    override fun renderer(
        typeDictionary: TypeDictionary,
        args: Writer
    ): Renderer<JsonGenerator> {
        return JavaxJsonStreamRenderer(typeDictionary, jsonProvider.createGenerator(args))
    }

    override fun resourceBody(
        body: HttpRequestBody,
        typeDictionary: TypeDictionary
    ): RequestBody {
        TODO("not implemented")
    }
}
