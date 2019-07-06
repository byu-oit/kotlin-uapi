package edu.byu.uapi.server.http.ktor

import edu.byu.uapi.server.http.HttpRequest
import io.ktor.application.ApplicationCall
import io.ktor.request.ApplicationRequest
import io.ktor.util.filter
import io.ktor.util.flattenEntries
import io.ktor.util.toMap

internal class KtorRequest(private val call: ApplicationCall) : HttpRequest {
    override val headers: Map<String, String> = call.request.headers.flattenEntries()
        .associate { it.first.toLowerCase() to it.second }

    override val pathParams: Map<String, String> = call.parameters
        .filter { k, _ -> k !in call.request.queryParameters }
        .flattenEntries()
        .toMap()

    override val queryParams: Map<String, List<String>> = call.request.queryParameters.toMap()
}

