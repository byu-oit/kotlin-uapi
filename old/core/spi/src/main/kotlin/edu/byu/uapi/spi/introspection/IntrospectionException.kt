package edu.byu.uapi.spi.introspection

class IntrospectionException(
    location: IntrospectionLocation,
    message: String,
    suggestions: List<String>
) : RuntimeException(toExceptionMessage(location, message, suggestions))

internal fun toExceptionMessage(
    location: IntrospectionLocation,
    message: String,
    suggestions: List<String>
): String {
    val suggestionStr = when {
        suggestions.isNotEmpty() -> suggestions.joinToString(prefix = "\n - ", separator = "\n - ")
        else -> ""
    }
    return "Error introspecting at $location: $message$suggestionStr"
}

