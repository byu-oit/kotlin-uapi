package edu.byu.uapi.spi.rendering

import edu.byu.uapi.spi.scalars.ScalarType
import edu.byu.uapi.spi.functional.onFailure
import kotlin.reflect.KClass

abstract class RendererBase<Self : RendererBase<Self, Output, Scalar>, Output : Any, Scalar> : Renderer<Output> {

    protected abstract fun newRenderer(): Self
    protected abstract val scalarRenderer: ScalarRenderer<Scalar>

    protected abstract fun addScalar(
        key: String,
        scalar: Scalar
    )

    protected abstract fun addScalarArray(
        key: String,
        items: Collection<Scalar>
    )

    protected abstract fun addTree(
        key: String,
        renderer: Self
    )

    protected abstract fun addEmptyArray(key: String)

    private fun <T : Any> scalarTypeFor(type: KClass<T>): ScalarType<T> {
        return typeDictionary.scalarConverter(type)
            .onFailure { throw it.asError() }
    }

    private fun <T : Any> renderScalar(value: T?): Scalar {
        return if (value == null) {
            scalarRenderer.nullValue()
        } else {
            @Suppress("UNCHECKED_CAST")
            val type = scalarTypeFor(value::class as KClass<T>)
            type.render(value, scalarRenderer)
        }
    }

    override fun value(
        key: String,
        value: Any?
    ) {
        addScalar(key, renderScalar(value))
    }

    override fun <T : Any> valueArray(
        key: String,
        values: Collection<T?>
    ) {
        addScalarArray(key, values.map(this::renderScalar))
    }

    override fun <T : Any> valueArray(
        key: String,
        values: Array<T?>
    ) {
        addScalarArray(key, values.map(this::renderScalar))
    }

    override fun valueArray(
        key: String,
        values: IntArray
    ) {
        addScalarArray(key, values.map { scalarRenderer.number(it) })
    }

    override fun valueArray(
        key: String,
        values: LongArray
    ) {
        addScalarArray(key, values.map { scalarRenderer.number(it) })
    }

    override fun valueArray(
        key: String,
        values: DoubleArray
    ) {
        addScalarArray(key, values.map { scalarRenderer.number(it) })
    }

    override fun valueArray(
        key: String,
        values: FloatArray
    ) {
        addScalarArray(key, values.map { scalarRenderer.number(it) })
    }

    override fun valueArray(
        key: String,
        values: BooleanArray
    ) {
        addScalarArray(key, values.map { scalarRenderer.boolean(it) })
    }

    override fun tree(
        key: String,
        tree: Renderable
    ) {
        addTree(key, renderTree(tree))
    }

    override fun tree(
        key: String,
        tree: Map<String, Any?>
    ) {
        addTree(key, newRenderer().also { it.mergeTree(tree) })
    }

    override fun tree(
        key: String,
        usingRenderer: Renderer<Output>.() -> Unit
    ) {
        addTree(key, newRenderer().apply { usingRenderer() })
    }

    private fun renderTree(tree: Renderable): Self = newRenderer().also(tree::render)

    override fun treeArray(
        key: String,
        array: Collection<Renderable>
    ) {
        addTreeArray(key, array.map(this::renderTree))
    }

    abstract fun addTreeArray(
        key: String,
        array: Collection<Self>
    )

    override fun treeArray(
        key: String,
        array: Array<Renderable>
    ) {
        addTreeArray(key, array.map(this::renderTree))
    }

    override fun mergeTree(
        usingRenderer: Renderer<Output>.() -> Unit
    ) {
        this.usingRenderer()
    }

    override fun mergeTree(
        tree: Renderable
    ) {
        tree.render(this)
    }

    override fun mergeTree(
        tree: Map<String, Any?>
    ) {
        tree.forEach { k, v ->
            when (v) {
                is Renderable -> this.tree(k, v)
                is Collection<*> -> {
                    TODO("Handle nested collections")
//                    if (v.isEmpty()) {
//                        nested.addEmptyArray(k)
//                    } else if (v.all { it == null }) {
//                        nested.addScalarArray(key, v.map { scalars.nullValue() })
//                    } else {
//                        v.forEach {
//
//                        }
//                    }
                }
                else -> this.value(k, v)
            }
        }
    }
}
