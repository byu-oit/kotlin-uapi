package edu.byu.uapidsl.examples.students

import edu.byu.uapidsl.adapters.openapi3.toOpenApi3Model
import edu.byu.uapidsl.adapters.openapi3.writeJsonTo
import java.io.BufferedWriter
import java.io.FileWriter
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("""
            Usage: OpeanAPIGen <output-file>
        """.trimIndent())
        exitProcess(1)
    }
    val destination = args.first()

    val model = personsModel.toOpenApi3Model()

    BufferedWriter(FileWriter(destination)).use {
        model.writeJsonTo(it)
    }
}
