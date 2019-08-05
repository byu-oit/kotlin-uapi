package edu.byu.uapi.server.http.engines

/**
 * The methods that the UAPI might map routes onto.
 */
enum class RouteMethod(
    /**
     * Whether or not, in UAPI, this request method might have a body.
     *
     * If the HTTP spec says a method does not have a body, this will be false. However, UAPI may define
     * more body-less methods than the HTTP spec does.
     */
    val mayHaveBody: Boolean
) {
    GET(false),
    POST(true),
    PUT(true),
    PATCH(true),
    DELETE(false) //Technically true in HTTP, but not in UAPI
}
