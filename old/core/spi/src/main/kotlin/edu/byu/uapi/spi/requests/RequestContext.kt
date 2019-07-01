package edu.byu.uapi.spi.requests

interface RequestContext {
    val baseUri: String
    val headers: Headers
    val fieldsets: FieldsetRequest?
}

data class FieldsetRequest(
    val requestedFieldsets: Set<String>,
    val requestedContexts: Set<String>
)
