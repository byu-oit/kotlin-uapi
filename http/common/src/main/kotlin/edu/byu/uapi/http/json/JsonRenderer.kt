package edu.byu.uapi.http.json

import edu.byu.uapi.server.inputs.TypeDictionary
import edu.byu.uapi.server.rendering.RendererBase
import edu.byu.uapi.server.rendering.ScalarRenderer
import java.math.BigDecimal
import java.math.BigInteger
import javax.json.Json
import javax.json.JsonObject
import javax.json.JsonObjectBuilder
import javax.json.JsonValue

class JsonRenderer(override val typeDictionary: TypeDictionary) : RendererBase<JsonRenderer, JsonObject, JsonValue>() {
    override fun newRenderer() = JsonRenderer(typeDictionary)

    override val scalarRenderer = JsonValueScalarRenderer

    private val root: JsonObjectBuilder = Json.createObjectBuilder()

    override fun addScalar(
        key: String,
        scalar: JsonValue
    ) {
        root.add(key, scalar)
    }

    override fun addScalarArray(
        key: String,
        items: Collection<JsonValue>
    ) {
        root.add(key, Json.createArrayBuilder().apply { items.forEach { add(it) } })
    }

    override fun addTree(
        key: String,
        renderer: JsonRenderer
    ) {
        root.add(key, renderer.root)
    }

    override fun addEmptyArray(key: String) {
        root.add(key, JsonValue.EMPTY_JSON_ARRAY)
    }

    override fun addTreeArray(
        key: String,
        array: Collection<JsonRenderer>
    ) {
        root.add(key, Json.createArrayBuilder().apply { array.forEach { add(it.root) } })
    }

    override fun render(): JsonObject = root.build()
}

object JsonValueScalarRenderer : ScalarRenderer<JsonValue> {
    override fun string(value: String): JsonValue = Json.createValue(value)

    override fun number(value: Int): JsonValue = Json.createValue(value)

    override fun number(value: Long): JsonValue = Json.createValue(value)

    override fun number(value: Float): JsonValue = Json.createValue(value.toDouble())

    override fun number(value: Double): JsonValue = Json.createValue(value)

    override fun number(value: Number): JsonValue {
        return when (value) {
            is Int -> this.number(value)
            is Long -> this.number(value)
            is Float -> this.number(value)
            is Double -> this.number(value)
            is BigInteger -> this.number(value.longValueExact())
            is BigDecimal -> this.number(value.toDouble())
            else -> this.number(value.toDouble())
        }
    }

    override fun boolean(value: Boolean): JsonValue {
        return if (value) {
            JsonValue.TRUE
        } else {
            JsonValue.FALSE
        }
    }

    override fun nullValue(): JsonValue = JsonValue.NULL

}
