package edu.byu.uapidsl.examples.students

import edu.byu.uapidsl.adapters.spark.igniteSpark
import edu.byu.uapidsl.adapters.openapi3.toOpenApi3Json

fun main(args: Array<String>) {
    val swagger = personsModel.toOpenApi3Json()
    personsModel.igniteSpark(8080)
}
