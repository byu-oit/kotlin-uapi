package edu.byu.uapi.spi.requests

interface Headers {
    operator fun get(header: String): Set<String>

    data class Simple(
        val headers: Map<String, Set<String>> = emptyMap()
    ): Headers {
        private val lowered = headers.mapKeys { it.key.toLowerCase() }.withDefault { emptySet() }
        override fun get(header: String): Set<String> {
            return lowered.getValue(header.toLowerCase())
        }
    }
}
