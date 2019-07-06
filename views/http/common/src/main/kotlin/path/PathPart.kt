package edu.byu.uapi.server.http.path

sealed class PathPart {
    internal abstract fun format(formatter: PathFormatter): String
}

sealed class VariablePathPart : PathPart() {
    override fun format(formatter: PathFormatter): String {
        return formatter.formatVariable(this)
    }
}

data class StaticPathPart(
    val part: String
) : PathPart() {
    override fun format(formatter: PathFormatter): String {
        return part
    }
}

data class SingleVariablePathPart(
    val name: String
) : VariablePathPart()

data class CompoundVariablePathPart(
    val names: List<String>
) : VariablePathPart()

fun staticPart(part: String): StaticPathPart {
    return StaticPathPart(part)
}

fun variablePart(name: String): SingleVariablePathPart {
    return SingleVariablePathPart(name)
}

fun variablePart(vararg names: String): CompoundVariablePathPart {
    return variablePart(names.toList())
}

fun variablePart(names: List<String>): CompoundVariablePathPart {
    return CompoundVariablePathPart(names)
}
