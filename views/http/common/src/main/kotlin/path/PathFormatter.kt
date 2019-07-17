package edu.byu.uapi.server.http.path

import edu.byu.uapi.server.http.errors.UAPIHttpMissingPathParamValueError

/**
 * Formats/unformats [PathPart]s to/from strings.
 *
 * For pre-baked path formatters, see [PathFormatters].
 *
 * Most of the time, you shouldn't implement this, but should extend [SimplePathFormatter] or [CompoundFlatteningFormatter].
 */
interface PathFormatter {

    /**
     * Given a variable, returns the corresponding path segment (without any slashes `/`).
     *
     * @param[part] the path part to format
     * @return the formatted path segment
     */
    fun formatVariable(part: VariablePathPart): String

    /**
     * Given a variable part and a map of raw path parameter values, returns a map which:
     *
     * - has the unformatted version of the variable name, in cases where the raw values still have the prefix and suffix
     * - includes only values defined for the given path part
     *
     * So, given a SingleVariablePathPart, the returned map will always have a single key-value mapping,
     * while a CompoundVariablePathPart will yield a map with as many key-value pairs as there are variable names.
     *
     * @param[part] the path part for which to get values
     * @param[values] the raw request values (param name -> value). The param name may be formatted or unformatted.
     * @return map containing the values for the given path part
     * @throws[UAPIHttpMissingPathParamValueError] if there is no value for a path parameter
     */
    @Throws(UAPIHttpMissingPathParamValueError::class)
    fun extractVariableValues(
        part: VariablePathPart,
        values: Map<String, String>
    ): Map<String, String>

    /**
     * Given a segment of a path, uses a heuristic to return a PathPart. This is the inverse of [formatVariable] -
     * in other words, the following test would pass:
     *
     * ```
     * val part = //some path part
     * val formatted = formatter.formatVariable(part)
     * val unformatted = formatter.unformat(formatted)
     *
     * assertEquals(part, unformatted)
     * ```
     *
     * @param[part] the path segment to unformat
     * @return the matching path part.
     */
    fun unformatPart(part: String): PathPart
}

/**
 * Extension function for formatting a list of path parts into a path string.
 */
fun PathFormatter.format(parts: RoutePath): String {
    return parts.joinToString(prefix = "/", separator = "/") { it.format(this) }
}

/**
 * Given a list of path parts and a map of raw values, passes each part through [PathFormatter#extractVariableValues]
 *
 * @param[parts] the path parts
 * @param[values] a map of 'dirty' path parts - names may be formatted, compound variables may be flattened, etc.
 * @return a map of variable names to values.
 */
fun PathFormatter.extractVariableValues(parts: RoutePath, values: Map<String, String>): Map<String, String> {
    return parts.filterIsInstance<VariablePathPart>().fold(mutableMapOf()) { acc, part ->
        acc += this.extractVariableValues(part, values)
        acc
    }
}

/**
 * Given a full path specification, including slashes '/', returns a list of parsed path parts.
 * This is the inverse of [PathFormatter.format].
 */
fun PathFormatter.unformat(pathSpec: String): List<PathPart> {
    return pathSpec
        .removePrefix("/")
        .removeSuffix("/")
        .split("/")
        .map { unformatPart(it) }
}
