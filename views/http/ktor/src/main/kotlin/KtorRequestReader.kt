package edu.byu.uapi.server.http.ktor

import edu.byu.uapi.server.http.engines.RequestReader
import io.ktor.application.ApplicationCall
import io.ktor.request.path
import io.ktor.request.receiveStream
import io.ktor.util.AttributeKey
import io.ktor.util.filter
import io.ktor.util.flattenEntries
import io.ktor.util.toMap
import java.io.InputStream

internal object KtorRequestReader: RequestReader<ApplicationCall> {

    override fun path(req: ApplicationCall): String = req.request.path()

    override fun headerNames(req: ApplicationCall): Set<String> {
        return req.getOrCacheHeaders().keys
    }

    override fun headerValue(req: ApplicationCall, name: String): String {
        return req.getOrCacheHeaders().getValue(name)
    }

    override fun queryParameters(req: ApplicationCall): Map<String, List<String>> {
        return req.getUapiQuery()
    }

    override fun pathParameters(req: ApplicationCall): Map<String, String> {
        return req.getUapiPath()
    }

    override suspend fun bodyStream(req: ApplicationCall): InputStream {
        return req.receiveStream()
    }
}

private fun ApplicationCall.getOrCacheHeaders(): Map<String, String> {
    return attributes.computeIfAbsent(CACHED_HEADERS_KEY) {
        request.headers.flattenEntries().toMap()
    }
}

private val CACHED_HEADERS_KEY = AttributeKey<Map<String, String>>(
    KtorRequestReader::class.qualifiedName + "#cached_headers_key"
)

private fun ApplicationCall.getUapiQuery() =
    request.queryParameters.toMap()

private fun ApplicationCall.getUapiPath() =
    parameters
        .filter { k, _ -> k !in request.queryParameters }
        .flattenEntries()
        .toMap()

