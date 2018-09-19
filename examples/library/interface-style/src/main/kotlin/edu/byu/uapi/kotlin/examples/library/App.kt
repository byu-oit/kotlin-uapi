package edu.byu.uapi.kotlin.examples.library

import edu.byu.uapi.kotlin.examples.library.impl.BookResource
import edu.byu.uapi.kotlin.examples.library.impl.MyUserContextFactory
import edu.byu.uapi.http.spark.startSpark
import edu.byu.uapi.server.UAPIRuntime

/**
 * Created by Scott Hutchings on 9/17/2018.
 * kotlin-uapi-dsl-pom
 */
fun main(args: Array<String>) {
    val runtime = UAPIRuntime(MyUserContextFactory())

    runtime.register("books", BookResource())

    runtime.startSpark()

}
