package edu.byu.uapi.server.http.errors

import edu.byu.uapi.server.spi.errors.UAPIMalformedRequestError

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
class UAPIHttpMissingBodyError(
    val whenRequired: String
): UAPIMalformedRequestError(
    "Missing request body. A request body is required when $whenRequired."
)
