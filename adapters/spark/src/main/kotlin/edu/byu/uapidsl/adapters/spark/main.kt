package edu.byu.uapidsl.adapters.spark

import edu.byu.uapidsl.UApiModel

fun <AuthContext: Any> UApiModel<AuthContext>.igniteSpark(port: Int = 4567) {
    println("In an ideal world, this would start Spark listening on port $port")
}
