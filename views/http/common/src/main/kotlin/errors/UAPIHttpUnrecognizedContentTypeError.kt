package edu.byu.uapi.server.http.errors

import edu.byu.uapi.server.spi.errors.UAPIUnsupportedMediaTypeError

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
class UAPIHttpUnrecognizedContentTypeError(
    val acceptableContentTypes: List<String>
): UAPIUnsupportedMediaTypeError(
    "Unable to process the provided Content-Type header. " +
        "Acceptable content types are ${acceptableContentTypes.joinToString()}"
)
