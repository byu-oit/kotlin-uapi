package edu.byu.uapi.server.types


typealias UAPILinks = Map<String, UAPILink>

data class UAPILink(
    val rel: String,
    val href: String,
    val method: LinkMethod = LinkMethod.GET
)

enum class LinkMethod{
    GET,
    PUT,
    POST,
    PATCH,
    DELETE,
    HEAD
}
