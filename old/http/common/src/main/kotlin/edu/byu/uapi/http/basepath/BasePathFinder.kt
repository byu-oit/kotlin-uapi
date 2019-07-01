package edu.byu.uapi.http.basepath

import edu.byu.uapi.http.HttpRequest
import java.net.URL

interface BasePathFinder {
    fun basePath(request: HttpRequest): URL?
}

class StaticBasePathFinder(val basePath: URL): BasePathFinder {
    override fun basePath(request: HttpRequest): URL = this.basePath
}

class XForwardedBasePathFinder(val basePath: URL): BasePathFinder {
    override fun basePath(request: HttpRequest): URL? {
        TODO("not implemented")
    }
}

//class DefaultBasePathFinder(): BasePathFinder {
//    override fun basePath(request: HttpRequest): URL? {
//
//    }
//}

//internal class OptionalJwtBasePathFinder() {
//    private val jwtOnClasspath: Boolean by lazy {
//        val jwtClass = Class.forName("edu.byu.jwt.")
//    }
//}
