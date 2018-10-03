package edu.byu.uapi.server.types

import edu.byu.uapi.server.rendering.Renderable
import edu.byu.uapi.server.rendering.Renderer

typealias UAPILinks = Map<String, UAPILink>

data class UAPILink(
    val rel: String,
    val href: String,
    val method: LinkMethod = LinkMethod.GET
) : Renderable {
    override fun render(renderer: Renderer<*>) {
        renderer.value("rel", rel)
        renderer.value("href", href)
        renderer.value("method", method)
    }
}

enum class LinkMethod {
    GET,
    PUT,
    POST,
    PATCH,
    DELETE,
    HEAD
}
