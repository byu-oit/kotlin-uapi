package edu.byu.uapi.http.json

import com.google.gson.Gson
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.rendering.Renderer
import java.io.Writer
import javax.json.spi.JsonProvider
import javax.json.stream.JsonGenerator
import com.google.gson.JsonObject as GsonObject
import javax.json.JsonObject as JavaxJsonObject

sealed class JsonEngine<Output: Any, Args> {
    abstract fun renderer(typeDictionary: TypeDictionary, args: Args): Renderer<Output>
}

object GsonTreeEngine: JsonEngine<GsonObject, Any?>() {
    override fun renderer(typeDictionary: TypeDictionary, args: Any?): Renderer<GsonObject> {
        return GsonTreeRenderer(typeDictionary, Gson())
    }
}

internal val jsonProvider: JsonProvider by lazy { JsonProvider.provider() }

object JavaxJsonTreeEngine: JsonEngine<JavaxJsonObject, Any?>() {
    override fun renderer(typeDictionary: TypeDictionary, args: Any?): Renderer<JavaxJsonObject> {
        return JavaxJsonTreeRenderer(typeDictionary, jsonProvider)
    }
}

object JavaxJsonStreamEngine: JsonEngine<JsonGenerator, Writer>() {
    override fun renderer(typeDictionary: TypeDictionary, args: Writer): Renderer<JsonGenerator> {
        return JavaxJsonStreamRenderer(typeDictionary, jsonProvider.createGenerator(args))
    }
}
