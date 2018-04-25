package edu.byu.uapidsl.examples.students

import edu.byu.uapidsl.adapters.spark.igniteSpark

fun main(args: Array<String>) {
    model().igniteSpark(8080)
}