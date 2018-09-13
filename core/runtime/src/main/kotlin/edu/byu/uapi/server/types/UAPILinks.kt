package edu.byu.uapi.server.types


typealias UAPILinks = Map<String, UAPILink>

data class UAPILink(
    val rel: String,
    val href: String,
    val method: LinkMethod = LinkMethod.GET
) {
//    fun toMap(): Map<String, Any?> {
//
//    }
}

//fun UAPILinks.toMap() = this.mapValues { it.value.toMap() }

enum class LinkMethod{
    GET,
    PUT,
    POST,
    PATCH,
    DELETE,
    HEAD
}
