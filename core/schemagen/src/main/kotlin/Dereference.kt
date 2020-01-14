package edu.byu.uapi.schemagen

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

internal fun dereferenceSchema(root: JsonNode): JsonNode {
    if (root !is ObjectNode) {
        return root
    }
    return generateSequence(root, ::runDerefPass)
        .take(20) // Stop spinning after 20 tries - prevent infinite loops
        .last()
}

private const val REF = "\$ref"

/**
 * Returns object if modified, null if not modified
 */
private fun runDerefPass(root: ObjectNode): ObjectNode? {
    val toFix = root.findParents(REF)
        .filterIsInstance<ObjectNode>()
    if (toFix.isEmpty()) {
        return null
    }

    var changed = false

    toFix.mapNotNull { n ->
        val ref = n[REF].textValue()
        if (ref.startsWith("#/")) {
            val refName = ref.drop(1)
            val refNode = root.at(refName) as? ObjectNode

            if (refNode != null) {
                n to refNode
            } else {
                null
            }
        } else {
            //We don't support non-local refs
            null
        }
    }.forEach { (n, ref) ->
        //Pre-emptively remove '$ref'
        n.remove(REF)
        changed = true

        ref.fieldNames().forEach { name ->
            // Don't override existing fields
            if (!n.has(name)) {
                n.set<JsonNode>(name, ref[name])
            }
        }
    }

    return if (changed) root else null
}
