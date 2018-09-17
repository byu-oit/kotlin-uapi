package edu.byu.uapi.server.types

typealias UAPILinks = Map<String, UAPILink>

data class UAPILink(
    val rel: String,
    val href: String,
    val method: LinkMethod = LinkMethod.GET
): UAPISerializable {
    override fun serialize(ser: SerializationStrategy) {
        ser.add("rel", rel)
        ser.add("href", href)
        ser.add("method", method)
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
