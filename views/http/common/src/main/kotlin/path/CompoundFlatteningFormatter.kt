package edu.byu.uapi.server.http.path

import edu.byu.uapi.server.http.errors.UAPIHttpMissingCompoundPathParamError

/**
 * A version of [SimplePathFormatter] which flattens compound variables into a single variable name.
 *
 * In many frameworks, you can't have multiple variable names in a single path segment. So, this combines them
 * all into one pseudo-variable, and can map backwards and forwards between the two versions.
 *
 * Handling of [SingleVariablePathPart] is unmodified from [SimplePathFormatter].
 *
 * By default, would turn a [CompoundVariablePathPart] with names of "foo", "bar", and "baz" into something like
 * "/{compound__foo__bar__baz}"
 *
 * @param[prefix] [SimplePathFormatter.prefix]
 * @param[suffix] [SimplePathFormatter.suffix]
 * @param[compoundPrefix] The prefix to put in front of the name of the flattened compound variable.
 * @param[compoundSeparator] The separator to put between the name of each variable in the compound part.
 */
open class CompoundFlatteningFormatter(
    prefix: String,
    suffix: String = "",
    val compoundPrefix: String = "compound__",
    val compoundSeparator: String = "__"
) : SimplePathFormatter(prefix, suffix) {

    /**
     * Flattens the given names into a single variable name.
     */
    override fun formatCompound(names: List<String>): String {
        return formatSingle(formatCompoundBare(names))
    }

    private fun formatCompoundBare(names: List<String>): String {
        return names.joinToString(prefix = compoundPrefix, separator = compoundSeparator)
    }

    /**
     * Un-flattens variable values.  This will find the value matching the formatted name of this
     * variable, the splits the values using the UAPI compound delimiter (',').
     */
    override fun extractCompoundVariableValues(names: List<String>, values: Map<String, String>): Map<String, String> {
        val bare = formatCompoundBare(names)

        val valueString = super.extractSingleVariableValues(bare, values)

        val splitValues = valueString.split(",")

        if (splitValues.size < names.size) {
            throw UAPIHttpMissingCompoundPathParamError(names)
        }

        return names.zip(splitValues).toMap()
    }

    /**
     * Un-flattens the variable names. If the flattened name doesn't start with [compoundPrefix], returns null.
     */
    override fun unformatCompoundVariable(part: String): CompoundVariablePathPart? {
        val single = unformatSingleVariableName(part) ?: return null
        if (!single.startsWith(compoundPrefix)) {
            return null
        }
        val names = single.removePrefix(compoundPrefix).split(compoundSeparator)
        return CompoundVariablePathPart(names)
    }
}
