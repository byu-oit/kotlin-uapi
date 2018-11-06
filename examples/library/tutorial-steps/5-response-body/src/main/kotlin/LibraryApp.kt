import edu.byu.uapi.http.spark.startSpark
import edu.byu.uapi.library.BooksResource
import edu.byu.uapi.library.LibraryUserContextFactory
import edu.byu.uapi.server.UAPIRuntime
import edu.byu.uapi.utilities.jwt.OnlyUseOnYourPersonalDevMachineBearerTokenUserContextFactory

//Look for system property or environment variable named 'ENV'
private val environment = System.getProperty("ENV") ?: System.getenv("ENV") ?: "production"
private val isLocalDevelopment = environment == "local"

fun main(args: Array<String>) {
    val libraryUserFactory = LibraryUserContextFactory()

    val actualUserFactory = if (isLocalDevelopment) {
        OnlyUseOnYourPersonalDevMachineBearerTokenUserContextFactory(libraryUserFactory)
    } else {
        libraryUserFactory
    }

    val runtime = UAPIRuntime(actualUserFactory)

    // All of our configuration is going to go here
    runtime.register("books", BooksResource())

    runtime.startSpark(
        port = 8080
    )
}
