import edu.byu.uapi.http.spark.startSpark
import edu.byu.uapi.library.LibraryUserContextFactory
import edu.byu.uapi.server.UAPIRuntime

fun main(args: Array<String>) {
    val runtime = UAPIRuntime(LibraryUserContextFactory())

    // All of our configuration is going to go here

    runtime.startSpark(
        port = 8080
    )
}
