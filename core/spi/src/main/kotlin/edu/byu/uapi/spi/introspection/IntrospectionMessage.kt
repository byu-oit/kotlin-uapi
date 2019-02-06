package edu.byu.uapi.spi.introspection

data class IntrospectionMessage(
    val severity: Severity,
    val location: IntrospectionLocation,
    val message: String,
    val suggestions: List<String> = emptyList()
) {
    enum class Severity {
        SUGGESTION,
        WARNING,
        ERROR;
    }

    companion object {
        fun suggestion(
            location: IntrospectionLocation,
            message: String,
            suggestions: List<String> = emptyList()
        ): IntrospectionMessage =
            IntrospectionMessage(
                Severity.SUGGESTION,
                location,
                message,
                suggestions
            )
    }

    private val stringifiedLines by lazy {
        val baseMessage = "[$severity] At $location: $message"
        if (suggestions.isEmpty()) {
            listOf(baseMessage)
        } else {
            val suggestionPadding = " ".repeat(severity.name.length + 3) // `[severity] `.length
            listOf(baseMessage) + suggestions.map { "$suggestionPadding- $it" }
        }
    }

    fun toMultilineString(linePrefix: String = ""): String {
        return stringifiedLines.joinToString(separator = "\n") { linePrefix + it }
    }
}
