package edu.byu.uapidsl.http

interface HttpResponse {

    val status: Int
    val headers: Headers
    val body: ResponseBody

}

interface ResponseBody {

    fun asString(): String

}
