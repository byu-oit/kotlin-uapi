package edu.byu.uapi.http.json

import com.google.gson.*
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.rendering.RendererBase
import edu.byu.uapi.spi.rendering.ScalarRenderer

class GsonTreeRenderer(override val typeDictionary: TypeDictionary, private val jsonProvider: Gson) : RendererBase<GsonTreeRenderer, JsonObject, JsonElement>() {
    override fun newRenderer() = GsonTreeRenderer(typeDictionary, jsonProvider)

    override val scalarRenderer = GsonScalarRenderer(jsonProvider)

    private val root: JsonObject = JsonObject()

    override fun addScalar(
        key: String,
        scalar: JsonElement
    ) {
        assert(scalar is JsonPrimitive || scalar is JsonNull)
        root.add(key, scalar)
    }

    override fun addScalarArray(
        key: String,
        items: Collection<JsonElement>
    ) {
        val array = JsonArray(items.size)
        items.forEach {
            assert(it is JsonPrimitive || it is JsonNull)
            array.add(it)
        }
        root.add(key, array)
    }

    override fun addTree(
        key: String,
        renderer: GsonTreeRenderer
    ) {
        root.add(key, renderer.root)
    }

    override fun addEmptyArray(key: String) {
        root.add(key, JsonArray(0))
    }

    override fun addTreeArray(
        key: String,
        array: Collection<GsonTreeRenderer>
    ) {
        val json = JsonArray(array.size).apply { array.forEach { add(it.root) } }
        root.add(key, json)
    }

    override fun render(): JsonObject = root
}

class GsonScalarRenderer(private val jsonProvider: Gson) : ScalarRenderer<JsonElement> {
    override fun string(value: String): JsonElement = JsonPrimitive(value)

    override fun number(value: Int): JsonElement = JsonPrimitive(value)

    override fun number(value: Long): JsonElement = JsonPrimitive(value)

    override fun number(value: Float): JsonElement = JsonPrimitive(value)

    override fun number(value: Double): JsonElement = JsonPrimitive(value)

    override fun number(value: Number): JsonElement = JsonPrimitive(value)

    override fun boolean(value: Boolean): JsonElement = JsonPrimitive(value)

    override fun nullValue(): JsonElement = JsonNull.INSTANCE
}
