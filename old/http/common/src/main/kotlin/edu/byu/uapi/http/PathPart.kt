package edu.byu.uapi.http

sealed class PathPart
data class StaticPathPart(
    val part: String
) : PathPart()

data class SimplePathVariablePart(
    val name: String
) : PathPart()

data class CompoundPathVariablePart(
    val names: List<String>
) : PathPart()


typealias PathParamDecorator = (part: String) -> String

object PathParamDecorators {
    val COLON: PathParamDecorator = { ":$it" }
    val CURLY_BRACE: PathParamDecorator = { "{$it}" }
    val NONE: PathParamDecorator = { it }
}

fun List<PathPart>.stringify(paramDecorator: PathParamDecorator): String {
    return this.stringify(paramDecorator) { part, pd ->
        part.names.joinToString(separator = ",", transform = pd)
    }
}

fun List<PathPart>.stringify(
    paramDecorator: PathParamDecorator,
    handleCompound: (CompoundPathVariablePart, PathParamDecorator) -> String
): String {
    return this.joinToString(separator = "/", prefix = "/") { part ->
        when (part) {
            is StaticPathPart -> part.part
            is SimplePathVariablePart -> paramDecorator(part.name)
            is CompoundPathVariablePart -> handleCompound(part, paramDecorator)
        }
    }
}
