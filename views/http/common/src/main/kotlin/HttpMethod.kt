package edu.byu.uapi.server.http

sealed class HttpMethod(
    val name: String,
    val allowsBodyInHttp: Boolean,
    val allowsBodyInUAPI: Boolean
) {

    sealed class Routable(
        name: String,
        allowsBodyInHttp: Boolean,
        allowsBodyInUAPI: Boolean
    ) : HttpMethod(name, allowsBodyInHttp, allowsBodyInUAPI) {
        constructor(name: String, allowsBody: Boolean) :
            this(name, allowsBody, allowsBody)

        object GET : Routable("GET", allowsBody = false)
        object PUT : Routable("PUT", allowsBody = true)
        object PATCH : Routable("PATCH", allowsBody = true)
        object POST : Routable("POST", allowsBody = true)
        object DELETE : Routable("DELETE", allowsBodyInHttp = true, allowsBodyInUAPI = false)

        companion object {
            // Lazy due to oddities in companion object init
            val values: Set<Routable> by lazy {
                setOf(GET, PUT, PATCH, POST, DELETE)
            }

            fun values() = values
        }
    }

    constructor(name: String, allowsBody: Boolean) : this(name, allowsBody, allowsBody)

    object HEAD : HttpMethod("HEAD", allowsBody = false)
    object OPTIONS : HttpMethod("OPTIONS", allowsBody = false)
    object TRACE : HttpMethod("TRACE", allowsBody = false)
    object CONNECT : HttpMethod("CONNECT", allowsBody = false)

    class Custom internal constructor(value: String) : HttpMethod(value, allowsBody = true)


    companion object {
        val GET by lazy { Routable.GET }
        val PUT by lazy { Routable.PUT }
        val PATCH by lazy { Routable.PATCH }
        val POST by lazy { Routable.POST }
        val DELETE by lazy { Routable.DELETE }

        // Lazy due to oddities in companion object init
        val values by lazy { Routable.values + setOf(HEAD, OPTIONS, TRACE, CONNECT) }

        fun values() = values

        private val knownMethodNames by lazy {
            values.associateBy {
                it.name
            }
        }

        operator fun invoke(name: String): HttpMethod {
            val upper = name.toUpperCase()
            return knownMethodNames.getOrElse(upper) {
                Custom(upper)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpMethod

        if (name != other.name) return false
        if (allowsBodyInHttp != other.allowsBodyInHttp) return false
        if (allowsBodyInUAPI != other.allowsBodyInUAPI) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + allowsBodyInHttp.hashCode()
        result = 31 * result + allowsBodyInUAPI.hashCode()
        return result
    }

    override fun toString(): String {
        return "HTTP $name"
    }
}
