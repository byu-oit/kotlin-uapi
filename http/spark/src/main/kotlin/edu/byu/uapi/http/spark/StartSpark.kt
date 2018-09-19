package edu.byu.uapi.http.spark

import edu.byu.uapi.server.UAPIRuntime
import spark.Spark

fun <UserContext: Any> UAPIRuntime<UserContext>.startSpark(
    port: Int = 4567
): Spark {
    TODO()
}
