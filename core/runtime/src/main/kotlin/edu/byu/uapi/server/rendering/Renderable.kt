package edu.byu.uapi.server.rendering

interface Renderable {
    fun render(renderer: Renderer<*>)
}

fun <R: Renderable> Map<String, R>.render(renderer: Renderer<*>) {
    this.forEach { k, v -> renderer.tree(k, v) }
}
