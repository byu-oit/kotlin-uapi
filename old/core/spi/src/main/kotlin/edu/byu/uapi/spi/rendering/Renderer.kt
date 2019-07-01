package edu.byu.uapi.spi.rendering

import edu.byu.uapi.spi.dictionary.TypeDictionary

interface Renderer<Output : Any> {
    val typeDictionary: TypeDictionary

    fun value(
        key: String,
        value: Any?
    )

    fun <T : Any> valueArray(
        key: String,
        values: Collection<T?>
    )

    fun <T : Any> valueArray(
        key: String,
        values: Array<T?>
    )

    fun valueArray(
        key: String,
        values: IntArray
    )

    fun valueArray(
        key: String,
        values: LongArray
    )

    fun valueArray(
        key: String,
        values: DoubleArray
    )

    fun valueArray(
        key: String,
        values: FloatArray
    )

    fun valueArray(
        key: String,
        values: BooleanArray
    )

    fun tree(
        key: String,
        usingRenderer: Renderer<Output>.() -> Unit
    )

    fun tree(
        key: String,
        tree: Renderable
    )

    fun tree(
        key: String,
        tree: Map<String, Any?>
    )

    fun treeArray(
        key: String,
        array: Collection<Renderable>
    )

    fun treeArray(
        key: String,
        array: Array<Renderable>
    )

    fun mergeTree(
        usingRenderer: Renderer<Output>.() -> Unit
    )

    fun mergeTree(
        tree: Renderable
    )

    fun mergeTree(
        tree: Map<String, Any?>
    )

    fun finalize(): Output
}
