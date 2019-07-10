package edu.byu.uapi.server.http.errors

import edu.byu.uapi.server.spi.errors.UAPIMalformedRequestError

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
class UAPIHttpMissingHeaderError(
    val headerName: String,
    val conditions: String? = null
): UAPIMalformedRequestError(
    buildMessage(headerName, conditions)
)

private fun buildMessage(headerName: String, conditions: String?): String {
    val msg = "Missing expected header '$headerName'."
    if (conditions == null) {
        return msg
    }
    return "$msg This header is required when $conditions."
}
