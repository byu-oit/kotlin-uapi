package lambda

import edu.byu.uapi.http.spark.startSpark
import org.pmw.tinylog.Configurator
import org.pmw.tinylog.Level

fun main() {
    Configurator.defaultConfig()
        .level("edu.byu", Level.DEBUG)
        .level(Level.WARNING)
        .activate()

    val model = helloWorldRuntime.model

    println(model)

    helloWorldRuntime.startSpark(8080)
}
