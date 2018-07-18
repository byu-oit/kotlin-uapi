package edu.byu.uapidsl.typemodeling

import com.fasterxml.jackson.databind.node.ObjectNode

typealias ObjectNodeCreator = () -> ObjectNode

fun ObjectNode.ensureObjectAtPath(path: List<String>): ObjectNode {
    return path.fold(this) { acc, part ->
        val found = acc.get(part)
        if (found != null) {
            //TODO(should probably, you know, check the type of this and throw a pretty exception)
            found as ObjectNode
        } else {
            acc.putObject(part)
        }
    }
}

fun ObjectNode.putArray(name: String, values: Iterable<String>) {
    val array = this.putArray(name)
    values.forEach { array.add(it) }
}
