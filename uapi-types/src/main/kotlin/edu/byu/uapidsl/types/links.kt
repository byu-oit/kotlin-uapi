package edu.byu.uapidsl.types

typealias UAPILinks = Map<String, UAPILink>

data class UAPILink(
    val rel: String,
    val href: String,
    val method: LinkMethod = LinkMethod.GET
)

enum class LinkMethod(
    override val serialized: String
) : ApiEnum {
    GET("GET"),
    PUT("PUT"),
    POST("POST"),
    PATCH("PATCH"),
    DELETE("DELETE"),
    HEAD("HEAD")
}
