package edu.byu.uapi.server.http.http4k._internal

import edu.byu.uapi.server.http.engines.RequestReader
import org.http4k.core.Request
import org.http4k.core.queries
import org.http4k.routing.RoutedRequest
import java.io.InputStream

object Http4kRequestReader : RequestReader<Request> {

    override fun path(req: Request): String {
        TODO("not implemented")
    }

    override fun headerNames(req: Request): Set<String> {
        return req.headers.mapTo(mutableSetOf()) { it.first }
    }

    override fun headerValue(req: Request, name: String): String {
        return req.header(name)!!
    }

    override fun queryParameters(req: Request): Map<String, List<String>> {
        return req.uri.queries().mapTo(mutableSetOf()) { it.first }
            .associateWith { req.queries(it).filterNotNull() }
    }

    override fun pathParameters(req: Request): Map<String, String> {
        return if (req is RoutedRequest) {
            req.xUriTemplate.extract(req.uri.path)
        } else {
            emptyMap()
        }
    }

    override suspend fun bodyStream(req: Request): InputStream {
        return req.body.stream
    }

}
