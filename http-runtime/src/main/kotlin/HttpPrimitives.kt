package edu.byu.uapidsl.http

enum class HttpMethod {
    GET, PUT, PATCH, POST, DELETE
}

interface QueryParams : Map<String, Set<String>>

class SimpleQueryParams(map: Map<String, Set<String>>) : QueryParams, Map<String, Set<String>> by map
object EmptyQueryParams : QueryParams, Map<String, Set<String>> by emptyMap()

interface Headers : Map<String, Set<String>>

class SimpleHeaders(map: Map<String, Set<String>>) : Headers, Map<String, Set<String>> by map {
    constructor(vararg headers: Pair<String, String>): this(headers.toMap().mapValues { setOf(it.value) })
}

object EmptyHeaders : Headers, Map<String, Set<String>> by emptyMap()

interface PathParams : Map<String, String>

class SimplePathParams(private val map: Map<String, String>) : PathParams, Map<String, String> by map {
    override fun toString(): String {
        return "SimplePathParams($map)"
    }
}
object EmptyPathParams : PathParams, Map<String, String> by emptyMap()

