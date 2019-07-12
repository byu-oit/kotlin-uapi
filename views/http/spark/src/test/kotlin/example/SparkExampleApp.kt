package edu.byu.uapi.server.http.spark.example

import edu.byu.uapi.server.http.spark.addUApiToSpark
import edu.byu.uapi.server.http.test.getTestApi
import spark.Service
import spark.Spark
import spark.Spark.get
import spark.Spark.path
import spark.Spark.port
import spark.route.HttpMethod
import spark.route.Routes
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

fun main() {
    port(4567)

    get("/") { req, resp ->
        "Hello!"
    }
    path("/api") {
        addUApiToSpark(getTestApi())
    }

    val routesProp =
        Service::class.declaredMemberProperties.first { it.name == "sharedRoutes" } as KProperty1<Service, Routes>

    routesProp.isAccessible = true

    val service = Spark::class.java.declaredMethods.first { it.name == "getInstance" }
        .run {
            isAccessible = true
            invoke(null) as Service
        }

    val routes = routesProp.get(service)

    val routeList = Routes::class.declaredMemberProperties.first { it.name == "sharedRoutes" }
        .apply { isAccessible = true }
        .get(routes) as List<*>

    routeList.forEach { println(it) }

//    val matches = sharedRoutes.findMultiple(HttpMethod.emptyGet, "/", "*/*")
//
//    matches.forEach {
//        it.apply {
//            println("$requestURI matches $matchUri $acceptType $target")
//        }
//    }

    Spark.stop()
    Spark.awaitStop()
}

