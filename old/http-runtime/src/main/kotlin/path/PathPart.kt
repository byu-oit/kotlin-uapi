package edu.byu.uapidsl.http.path

sealed class PathPart

data class StaticPathPart(
    val part: String
): PathPart()

data class SimplePathVariablePart(
    val name: String
): PathPart()

data class CompoundPathVariablePart(
    val names: List<String>
): PathPart()
