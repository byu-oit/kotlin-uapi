package edu.byu.uapi.spi.requests

interface RequestContext {
    val baseUri: String
    val headers: Map<String, Set<String>>
}
