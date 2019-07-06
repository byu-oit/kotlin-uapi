package edu.byu.uapi.server.http.path

import edu.byu.uapi.server.http.errors.UAPIHttpInternalError

open class SimplePathFormatter(val prefix: String, val suffix: String = "") : PathFormatter {
    override fun formatVariable(part: VariablePathPart): String {
        return when (part) {
            is SingleVariablePathPart   -> formatSingle(part.name)
            is CompoundVariablePathPart -> formatCompound(part.names)
        }
    }

    protected open fun formatSingle(name: String): String {
        return prefix + name + suffix
    }

    protected open fun formatCompound(names: List<String>): String {
        return names.joinToString(",", transform = this::formatSingle)
    }

    override fun unformatVariableValues(
        part: VariablePathPart,
        values: Map<String, String>
    ): Map<String, String> {
        return when(part) {
            is SingleVariablePathPart   -> mapOf(part.name to unformatSingle(part.name, values))
            is CompoundVariablePathPart -> unformatCompound(part.names, values)
        }
    }

    protected open fun unformatSingle(name: String, values: Map<String, String>): String {
        return values[name]
            ?: values[formatSingle(name)]
            ?: throw UAPIHttpInternalError("Missing value for path parameter '$name'")
    }

    protected open fun unformatCompound(names: List<String>, values: Map<String, String>): Map<String, String> {
        return names.associateWith { unformatSingle(it, values) }
    }

}
