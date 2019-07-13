package edu.byu.uapi.server.http.path

interface PathFormatter {
    fun formatVariable(part: VariablePathPart): String
    fun extractVariableValues(
        part: VariablePathPart,
        values: Map<String, String>
    ): Map<String, String>

    fun unformat(part: String): PathPart
}

object PathFormatters {
    val COLON: PathFormatter = SimplePathFormatter(":")
    val CURLY_BRACE: PathFormatter = SimplePathFormatter("{", "}")
    val FLAT_COLON: PathFormatter = CompoundFlatteningFormatter(":")
    val FLAT_CURLY_BRACE: PathFormatter = CompoundFlatteningFormatter("{", "}")
}

fun PathFormatter.format(parts: RoutePath): String {
    return parts.joinToString(prefix = "/", separator = "/") { it.format(this) }
}

fun PathFormatter.unformatValues(parts: RoutePath, values: Map<String, String>): Map<String, String> {
    return parts.filterIsInstance<VariablePathPart>().fold(mutableMapOf()) { acc, part ->
        acc += this.extractVariableValues(part, values)
        acc
    }
}
