package edu.byu.uapi.spi.rendering

import edu.byu.uapi.spi.dictionary.TypeDictionary

class MapRenderer(override val typeDictionary: TypeDictionary) : RendererBase<MapRenderer, Map<String, Any?>, Any?>() {

    internal val map = LinkedHashMap<String, Any?>()

    override val scalarRenderer = SimpleScalarRenderer
    override fun newRenderer() = MapRenderer(typeDictionary)

    override fun addScalar(
        key: String,
        scalar: Any?
    ) {
        map[key] = scalar
    }

    override fun addScalarArray(
        key: String,
        items: Collection<Any?>
    ) {
        map[key] = items
    }

    override fun addTree(
        key: String,
        renderer: MapRenderer
    ) {
        map[key] = renderer.map
    }

    override fun addEmptyArray(key: String) {
        map[key] = emptyList<Any>()
    }

    override fun addTreeArray(
        key: String,
        array: Collection<MapRenderer>
    ) {
        map[key] = array.map(MapRenderer::map)
    }

    // TODO: Defensively copy the contents
    override fun finalize(): Map<String, Any?> = LinkedHashMap(map)

}
