package edu.byu.uapi.spi.rendering

interface Renderable {
    fun render(renderer: Renderer<*>)
}

fun <R: Renderable> Map<String, R>.render(renderer: Renderer<*>) {
    this.forEach { k, v -> renderer.tree(k, v) }
}
