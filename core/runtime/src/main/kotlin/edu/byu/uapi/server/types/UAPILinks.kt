package edu.byu.uapi.server.types

import edu.byu.uapi.server.serialization.TreeSerializationStrategy
import edu.byu.uapi.server.serialization.UAPISerializableTree

typealias UAPILinks = Map<String, UAPILink>

data class UAPILink(
    val rel: String,
    val href: String,
    val method: LinkMethod = LinkMethod.GET
): UAPISerializableTree {
    override fun serialize(strategy: TreeSerializationStrategy) {
        strategy.string("rel", rel)
        strategy.string("href", href)
        strategy.enum("method", method)
    }
}

enum class LinkMethod{
    GET,
    PUT,
    POST,
    PATCH,
    DELETE,
    HEAD
}
