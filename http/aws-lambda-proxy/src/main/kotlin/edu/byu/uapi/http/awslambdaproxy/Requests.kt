package edu.byu.uapi.http.awslambdaproxy

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import edu.byu.uapi.http.*
import edu.byu.uapi.spi.requests.Headers

class LambdaRequest(
    req: APIGatewayProxyRequestEvent,
    pattern: Regex,
    paramNames: List<String>
) : HttpRequest {
    override val method: HttpMethod = HttpMethod.valueOf(req.httpMethod.toUpperCase())
    override val rawPath: String
        get() = TODO("not implemented")
    override val body: RequestBody?
        get() = TODO("not implemented")
    override val path: HttpPathParams = extractPathParams(req, pattern, paramNames)
    override val headers: Headers = LambdaHeaders(req)
    override val query: HttpQueryParams = req.multiValueQueryStringParameters.orEmpty().mapValues { it.value.toSet() }
}

fun extractPathParams(
    request: APIGatewayProxyRequestEvent,
    pattern: Regex,
    paramsNames: List<String>
): HttpPathParams {
    val groupValues = pattern.matchEntire(request.path)!!.groupValues
    return paramsNames
        .zip(groupValues.subList(1, groupValues.size)) { k, v -> k to v }
        .toMap()
}

class LambdaHeaders(private val req: APIGatewayProxyRequestEvent) : Headers {
    override fun get(header: String): Set<String> {
        return req.multiValueHeaders.orEmpty()[header.toLowerCase()]?.toSet() ?: emptySet()
    }
}
