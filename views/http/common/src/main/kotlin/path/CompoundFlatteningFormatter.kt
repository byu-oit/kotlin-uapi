package edu.byu.uapi.server.http.path

import edu.byu.uapi.server.http.errors.UAPIHttpMissingCompoundPathParamError

open class CompoundFlatteningFormatter(
    prefix: String,
    suffix: String = "",
    val compoundPrefix: String = "compound__",
    val compoundSeparator: String = "__"
) : SimplePathFormatter(prefix, suffix) {
    override fun formatCompound(names: List<String>): String {
        return formatSingle(formatCompoundBare(names))
    }

    private fun formatCompoundBare(names: List<String>): String {
        return names.joinToString(prefix = compoundPrefix, separator = compoundSeparator)
    }

    override fun extractCompoundVariableValues(names: List<String>, values: Map<String, String>): Map<String, String> {
        val bare = formatCompoundBare(names)

        val valueString = super.extractSingleVariableValues(bare, values)

        val splitValues = valueString.split(",")

        if (splitValues.size < names.size) {
            throw UAPIHttpMissingCompoundPathParamError(names)
        }

        return names.zip(splitValues).toMap()
    }

    override fun unformatCompoundVariable(part: String): CompoundVariablePathPart? {
        val single = unformatSingleVariableName(part) ?: return null
        val names = single.removePrefix(compoundPrefix).split(compoundSeparator)
        return CompoundVariablePathPart(names)
    }
}
