package edu.byu.uapi.server.http.path

interface PathFormatter {
    fun formatVariable(part: VariablePathPart): String
    fun unformatVariableValues(
        part: VariablePathPart,
        values: Map<String, String>
    ): Map<String, String>
}

object PathFormatters {
    val COLON: PathFormatter = SimplePathFormatter(":")
    val CURLY_BRACE: PathFormatter = SimplePathFormatter("{", "}")
    val FLAT_COLON: PathFormatter = CompoundFlatteningFormatter(":")
    val FLAT_CURLY_BRACE: PathFormatter = CompoundFlatteningFormatter("{", "}")
}

fun PathFormatter.format(parts: List<PathPart>): String {
    return parts.joinToString(prefix = "/", separator = "/") { it.format(this) }
}
