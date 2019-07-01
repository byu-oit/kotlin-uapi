package edu.byu.uapi.server.types

import java.net.URL
import java.net.URLEncoder

interface LinkBuilder {
    fun addPath(parts: List<String>)
    fun addQuery(query: Map<String, Set<String>>)

    fun build(): URL
}

interface LinkBuilderFactory {
    fun fromRequestBase(): LinkBuilder
}

class DefaultLinkBuilder(val baseUrl: URL) : LinkBuilder {
    private val path = mutableListOf<String>()
    private val query = mutableMapOf<String, Set<String>>()

    override fun addPath(parts: List<String>) {
        this.path.addAll(parts)
    }

    override fun addQuery(query: Map<String, Set<String>>) {
        this.query.putAll(query)
    }

    override fun build(): URL {
        return URL(baseUrl,
                         path.joinToString("/")
                             + if (query.isNotEmpty()) query.asSequence()
                             .joinToString(separator = "&", prefix = "?") { q ->
                                 q.key.urlEncode() + '=' + q.value.joinToString(",") { it.urlEncode() }
                             } else ""
        )
    }

}

private fun String.urlEncode() = URLEncoder.encode(this, "UTF-8")
