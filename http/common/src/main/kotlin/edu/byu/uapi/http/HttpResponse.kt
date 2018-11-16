package edu.byu.uapi.http

import edu.byu.uapi.spi.rendering.Renderer

interface HttpResponse {

    val status: Int
    val headers: Map<String, Set<String>>
    val body: ResponseBody

}

interface ResponseBody {

    fun <Output: Any> render(renderer: Renderer<Output>): Output

}

object EmptyResponseBody : ResponseBody {
    override fun <Output : Any> render(renderer: Renderer<Output>): Output = renderer.finalize()
}
