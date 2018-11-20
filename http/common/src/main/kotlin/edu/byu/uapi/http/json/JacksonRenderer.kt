package edu.byu.uapi.http.json

import com.fasterxml.jackson.core.JsonGenerator
import edu.byu.uapi.server.spi.requireScalarType
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.rendering.Renderable
import edu.byu.uapi.spi.rendering.Renderer
import edu.byu.uapi.spi.rendering.ScalarRenderer
import edu.byu.uapi.spi.scalars.ScalarType
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass

class JacksonRenderer(override val typeDictionary: TypeDictionary, private val json: JsonGenerator) : Renderer<JsonGenerator> {
    private val scalarRenderer = JacksonScalarRenderer(json)

    init {
        json.writeStartObject()
    }

    override fun value(
        key: String,
        value: Any?
    ) {
        json.writeFieldName(key)
        writeScalar(value)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Any> writeScalar(value: T?) {
        if (value == null) {
            scalarRenderer.nullValue()
        } else {
            val klass = value::class as KClass<T>
            val scalar: ScalarType<T> = typeDictionary.requireScalarType(klass)
            scalar.render(value, scalarRenderer)
        }
    }

    override fun <T : Any> valueArray(
        key: String,
        values: Collection<T?>
    ) {
        array(key) {
            values.forEach { writeScalar(it) }
        }
    }

    override fun <T : Any> valueArray(
        key: String,
        values: Array<T?>
    ) {
        array(key) {
            values.forEach { writeScalar(it) }
        }
    }

    override fun valueArray(
        key: String,
        values: IntArray
    ) {
        array(key) {
            values.forEach { scalarRenderer.number(it) }
        }
    }

    override fun valueArray(
        key: String,
        values: LongArray
    ) {
        array(key) {
            values.forEach { scalarRenderer.number(it) }
        }
    }

    override fun valueArray(
        key: String,
        values: DoubleArray
    ) {
        array(key) {
            values.forEach { scalarRenderer.number(it) }
        }
    }

    override fun valueArray(
        key: String,
        values: FloatArray
    ) {
        array(key) {
            values.forEach { scalarRenderer.number(it) }
        }
    }

    override fun valueArray(
        key: String,
        values: BooleanArray
    ) {
        array(key) {
            values.forEach { scalarRenderer.boolean(it) }
        }
    }

    private inline fun array(key: String, populate: () -> Unit) {
        json.writeArrayFieldStart(key)
        populate()
        json.writeEndArray()
    }

    override fun tree(
        key: String,
        usingRenderer: Renderer<JsonGenerator>.() -> Unit
    ) {
        writeTree(key) {
            this.usingRenderer()
        }
    }

    private inline fun writeTree(key: String, write: () -> Unit) {
        json.writeObjectFieldStart(key)
        write()
        json.writeEndObject()
    }

    override fun tree(
        key: String,
        tree: Renderable
    ) {
        writeTree(key) {
            tree.render(this)
        }
    }

    override fun tree(
        key: String,
        tree: Map<String, Any?>
    ) {
        writeTree(key) {
            this.mergeTree(tree)
        }
    }

    override fun treeArray(
        key: String,
        array: Collection<Renderable>
    ) {
        array(key) {
            array.forEach {
                json.writeStartObject()
                it.render(this)
                json.writeEndObject()
            }
        }
    }

    override fun treeArray(
        key: String,
        array: Array<Renderable>
    ) {
        array(key) {
            array.forEach {
                json.writeStartObject()
                it.render(this)
                json.writeEndObject()
            }
        }
    }

    override fun mergeTree(usingRenderer: Renderer<JsonGenerator>.() -> Unit) {
        this.usingRenderer()
    }

    override fun mergeTree(tree: Renderable) {
        tree.render(this)
    }

    override fun mergeTree(tree: Map<String, Any?>) {
        tree.forEach { k, v ->
            when (v) {
                is Renderable -> this.tree(k, v)
                is Collection<*> -> {
                    TODO("Handle nested collections")
                }
                else -> this.value(k, v)
            }
        }
    }

    lateinit var finalized: Any

    override fun finalize(): JsonGenerator {
        if (this::finalized.isInitialized) {
            return json
        }
        this.finalized = Any()
        json.writeEndObject()
        json.flush()
        return json
    }
}

class JacksonScalarRenderer(private val json: JsonGenerator): ScalarRenderer<Unit> {
    override fun string(value: String) {
        json.writeString(value)
    }

    override fun number(value: Int) {
        json.writeNumber(value)
    }

    override fun number(value: Long) {
        json.writeNumber(value)
    }

    override fun number(value: Float) {
        json.writeNumber(value)
    }

    override fun number(value: Double) {
        json.writeNumber(value)
    }

    override fun number(value: Number) {
        when (value) {
            is Int -> this.number(value)
            is Long -> this.number(value)
            is Float -> this.number(value)
            is Double -> this.number(value)
            is BigInteger -> json.writeNumber(value)
            is BigDecimal -> json.writeNumber(value)
            else -> this.number(value.toDouble())
        }
    }

    override fun boolean(value: Boolean) {
        json.writeBoolean(value)
    }

    override fun nullValue() {
        json.writeNull()
    }
}
