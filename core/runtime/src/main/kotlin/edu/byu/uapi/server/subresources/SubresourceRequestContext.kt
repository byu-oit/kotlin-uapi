package edu.byu.uapi.server.subresources

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

interface SubresourceRequestContext {
    val allRequestedSubresources: Set<String>
    val attributes: ConcurrentMap<String, Any?>

    class Simple(
        override val allRequestedSubresources: Set<String> = emptySet(),
        override val attributes: ConcurrentMap<String, Any?> = ConcurrentHashMap()
    ) : SubresourceRequestContext
}
