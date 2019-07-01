package edu.byu.uapi.server.resources

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

interface ResourceRequestContext {
    val requestedSubresources: Set<String>
    val attributes: ConcurrentMap<String, Any?>

    class Simple(
        override val requestedSubresources: Set<String>,
        override val attributes: ConcurrentMap<String, Any?> = ConcurrentHashMap()
    ) : ResourceRequestContext
}
