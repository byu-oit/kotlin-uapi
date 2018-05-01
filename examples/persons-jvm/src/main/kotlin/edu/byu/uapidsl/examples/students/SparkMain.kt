package edu.byu.uapidsl.examples.students

import edu.byu.uapidsl.adapters.spark.igniteSpark

fun main(args: Array<String>) {
  personsModel.igniteSpark(8080)
  println(personsModel)
}
