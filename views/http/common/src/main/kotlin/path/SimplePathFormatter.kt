package edu.byu.uapi.server.http.path

import edu.byu.uapi.server.http.errors.UAPIHttpInternalError
import org.intellij.lang.annotations.Language

const val COMPOUND_PARAMETER_SEPARATOR = ","

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
        return names.joinToString(COMPOUND_PARAMETER_SEPARATOR, transform = this::formatSingle)
    }

    override fun unformat(part: String): PathPart {
        return unformatCompoundVariable(part)
            ?: unformatSingleVariable(part)
            ?: StaticPathPart(part)
    }

    @Language("RegExp")
    private val variableGroup = """(?:${Regex.escape(prefix)}([_a-zA-Z0-9]+)${Regex.escape(suffix)})"""
    private val variableGroupRegex = variableGroup.toRegex()
    private val singleVariableRegex = "^$variableGroup\$".toRegex()
    private val compoundVariableRegex = "^(?:$variableGroup,)+$variableGroup\$".toRegex()

    protected open fun unformatSingleVariable(part: String): SingleVariablePathPart? {
        return unformatSingleVariableName(part)?.let { SingleVariablePathPart(it) }
    }

    protected fun unformatSingleVariableName(part: String): String? {
        return singleVariableRegex.matchEntire(part)?.let { unwrapName(it) }
    }

    private fun unwrapName(matchResult: MatchResult): String {
        return matchResult.groupValues.last()
    }

    protected open fun unformatCompoundVariable(part: String): CompoundVariablePathPart? {
        if (!part.matches(compoundVariableRegex)) {
            return null
        }
        val names = variableGroupRegex.findAll(part).map { unwrapName(it) }
            .toList()
        return CompoundVariablePathPart(names)
    }

    override fun extractVariableValues(
        part: VariablePathPart,
        values: Map<String, String>
    ): Map<String, String> {
        return when (part) {
            is SingleVariablePathPart   -> mapOf(part.name to extractSingleVariableValues(part.name, values))
            is CompoundVariablePathPart -> extractCompoundVariableValues(part.names, values)
        }
    }

    protected open fun extractSingleVariableValues(name: String, values: Map<String, String>): String {
        return values[name]
            ?: values[formatSingle(name)]
            ?: throw UAPIHttpInternalError("Missing name for path parameter '$name'")
    }

    protected open fun extractCompoundVariableValues(
        names: List<String>,
        values: Map<String, String>
    ): Map<String, String> {
        return names.associateWith { extractSingleVariableValues(it, values) }
    }

}
