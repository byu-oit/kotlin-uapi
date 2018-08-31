package http

interface HttpResponse {

    val status: Int
    val headers: Headers
    val body: ResponseBody

}

interface ResponseBody {

    fun asString(): String

}

object EmptyResponseBody: ResponseBody {
    override fun asString(): String {
        return ""
    }
}
