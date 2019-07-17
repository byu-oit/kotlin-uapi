package edu.byu.uapi.server.http.path

import edu.byu.uapi.server.http.errors.UAPIHttpInternalError
import edu.byu.uapi.server.http.errors.UAPIHttpMissingPathParamValueError
import org.intellij.lang.annotations.Language

const val COMPOUND_PARAMETER_SEPARATOR = ","

/**
 * Basic implementation of [PathFormatter]. Wraps all variable names in the given
 * [prefix] and [suffix].
 *
 * @param[prefix] prefix to put before all variable names.
 * @param[suffix] optional suffix. Defaults to "" (empty string).
 */
@Suppress("TooManyFunctions")
open class SimplePathFormatter(val prefix: String, val suffix: String = "") : PathFormatter {

    /**
     * Implementation of [PathFormatter.formatVariable].
     *
     * Depending on which type of variable this is, will invoke either
     * [formatSingle] or [formatCompound], allowing the default behaviors to be customized.
     */
    override fun formatVariable(part: VariablePathPart): String {
        return when (part) {
            is SingleVariablePathPart   -> formatSingle(part.name)
            is CompoundVariablePathPart -> formatCompound(part.names)
        }
    }

    //<editor-fold desc="formatVariable implementation" defaultstate="collapsed">

    /**
     * Format a single-variable path part with the given name into a path segment.
     * By default, wraps the given [name] with the prefix and suffix.
     *
     * Override this to customize how both single variable parts and the individual parts of a compound variable
     * are formatted.
     */
    protected open fun formatSingle(name: String): String {
        return prefix + name + suffix
    }

    /**
     * Formats a compound variable with the given [names] (in order) into a path segment.
     * By default, passes each name to [formatSingle], then joins them together with a comma `,`.
     */
    protected open fun formatCompound(names: List<String>): String {
        return names.joinToString(COMPOUND_PARAMETER_SEPARATOR, transform = this::formatSingle)
    }

    //</editor-fold>

    /**
     * Implementation of [PathFormatter.unformatPart]
     *
     * By default, it tries, in succession, [unformatCompoundVariable], then [unformatSingleVariable], then
     * defaults to constructing a static path part.
     */
    override fun unformatPart(part: String): PathPart {
        return unformatCompoundVariable(part)
            ?: unformatSingleVariable(part)
            ?: StaticPathPart(part)
    }

    //<editor-fold desc="unformatPart implementation" defaultstate="collapsed">

    @Language("RegExp")
    private val variableGroup = """(?:${Regex.escape(prefix)}([_a-zA-Z0-9]+)${Regex.escape(suffix)})"""
    private val variableGroupRegex = variableGroup.toRegex()
    private val singleVariableRegex = "^$variableGroup\$".toRegex()
    private val compoundVariableRegex = "^(?:$variableGroup,)+$variableGroup\$".toRegex()

    /**
     * Unformats a single path variable, or returns null if the given string does not represent one.
     */
    protected open fun unformatSingleVariable(part: String): SingleVariablePathPart? {
        return unformatSingleVariableName(part)?.let { SingleVariablePathPart(it) }
    }

    /**
     * Unformats a single path name. By default, if the given string starts and ends with the
     * value of [prefix] and [suffix], it will return the string minus the prefix and suffix.
     *
     * Override this to change the detection and unwrapping of variable names.
     */
    protected fun unformatSingleVariableName(part: String): String? {
        return singleVariableRegex.matchEntire(part)?.let { unwrapName(it) }
    }

    private fun unwrapName(matchResult: MatchResult): String {
        return matchResult.groupValues.last()
    }

    /**
     * Unformats a compund variable, or returns null if the given string doesn't represent one.
     *
     * By default, sees if there are multiple matching variable name patterns, separated by a comma,
     * then passes each matching name to `#unwrapName`.
     *
     * Override this to change the detection and unwrapping of compound variables.
     */
    protected open fun unformatCompoundVariable(part: String): CompoundVariablePathPart? {
        if (!part.matches(compoundVariableRegex)) {
            return null
        }
        val names = variableGroupRegex.findAll(part).map { unwrapName(it) }
            .toList()
        return CompoundVariablePathPart(names)
    }

    //</editor-fold>

    /**
     * Implementation of [PathFormatter.extractVariableValues]
     *
     * By default, this delegates to [extractSingleVariableValues] and [extractCompoundVariableValues].
     */
    @Throws(UAPIHttpMissingPathParamValueError::class)
    override fun extractVariableValues(
        part: VariablePathPart,
        values: Map<String, String>
    ): Map<String, String> {
        return when (part) {
            is SingleVariablePathPart   -> mapOf(part.name to extractSingleVariableValues(part.name, values))
            is CompoundVariablePathPart -> extractCompoundVariableValues(part.names, values)
        }
    }

    //<editor-fold desc="extractVariableValues implementation" defaultstate="collapsed">

    /**
     * Extracts a single variable value from a map of values.
     *
     * By default, tries to retrieve the given name from the values map,
     * then tries formatting the name using [formatSingle], then throws a UAPIHttpInternalError.
     */
    @Throws(UAPIHttpMissingPathParamValueError::class)
    protected open fun extractSingleVariableValues(name: String, values: Map<String, String>): String {
        return values[name]
            ?: values[formatSingle(name)]
            ?: throw UAPIHttpInternalError("Missing name for pathSpec parameter '$name'")
    }

    /**
     * Extracts compound variable values from a map of values.
     *
     * By default, calls [extractSingleVariableValues] for each name in the list, then
     * combines the resulting values.
     */
    @Throws(UAPIHttpMissingPathParamValueError::class)
    protected open fun extractCompoundVariableValues(
        names: List<String>,
        values: Map<String, String>
    ): Map<String, String> {
        return names.associateWith { extractSingleVariableValues(it, values) }
    }
    //</editor-fold>

}
