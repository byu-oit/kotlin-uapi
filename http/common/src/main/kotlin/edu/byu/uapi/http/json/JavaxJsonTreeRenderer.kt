package edu.byu.uapi.http.json

import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.rendering.RendererBase
import edu.byu.uapi.spi.rendering.ScalarRenderer
import java.math.BigDecimal
import java.math.BigInteger
import javax.json.JsonObject
import javax.json.JsonObjectBuilder
import javax.json.JsonValue
import javax.json.spi.JsonProvider

class JavaxJsonTreeRenderer(override val typeDictionary: TypeDictionary, private val jsonProvider: JsonProvider) : RendererBase<JavaxJsonTreeRenderer, JsonObject, JsonValue>() {
    override fun newRenderer() = JavaxJsonTreeRenderer(typeDictionary, jsonProvider)

    override val scalarRenderer = JsonValueScalarRenderer(jsonProvider)

    private val root: JsonObjectBuilder = jsonProvider.createObjectBuilder()

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
        root.add(key, jsonProvider.createArrayBuilder().apply { items.forEach { add(it) } })
    }

    override fun addTree(
        key: String,
        renderer: JavaxJsonTreeRenderer
    ) {
        root.add(key, renderer.root)
    }

    override fun addEmptyArray(key: String) {
        root.add(key, JsonValue.EMPTY_JSON_ARRAY)
    }

    override fun addTreeArray(
        key: String,
        array: Collection<JavaxJsonTreeRenderer>
    ) {
        root.add(key, jsonProvider.createArrayBuilder().apply { array.forEach { add(it.root) } })
    }

    override fun render(): JsonObject = root.build()
}

class JsonValueScalarRenderer(private val jsonProvider: JsonProvider) : ScalarRenderer<JsonValue> {
    override fun string(value: String): JsonValue = jsonProvider.createValue(value)

    override fun number(value: Int): JsonValue = jsonProvider.createValue(value)

    override fun number(value: Long): JsonValue = jsonProvider.createValue(value)

    override fun number(value: Float): JsonValue = jsonProvider.createValue(value.toDouble())

    override fun number(value: Double): JsonValue = jsonProvider.createValue(value)

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
