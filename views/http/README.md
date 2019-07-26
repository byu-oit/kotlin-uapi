# UAPI HTTP RESTful views

This package contains all of the logic to map a UAPI resource model to a fully-functional RESTful HTTP service.

There are integrations available for multiple HTTP frameworks. See each README for usage details.

* [spark](spark/README.md) - Based on [Spark Java](http://sparkjava.com).
* [ktor](ktor/README.md) - Based on Jetbrains' [ktor Server](http://ktor.io) - Work in progress

# Other packages

* [common](common/README.md) - Provides the bindings between a UAPI model and the underlying HTTP framework.
* [common-test](common-test/README.md) - Utilities for testing HTTP framework integrations for compliance with the common module.

# How does all of this fit together?

First, the developer defines a UAPI Model (TODO: Actual model class?) defining all of their resources.

Then, the developer adds an HTTP engine implementation to their project.

The developer starts the HTTP server in whatever way the framework normally works.

While defining the routes in their HTTP framework, the developer calls on of the UAPI entrypoints for the framework,
passing in the UAPI Model.

The HTTP framework integration then gets a list of routes from the UAPI Model and adds them to the framework's router.

The framework will now feed requests to the paths defined in the model to the appropriate developer code.

## Startup Flow

When the framework entrypoints are invoked, they receive an [`HttpRouteSource`](common/src/main/kotlin/engines/HttpRouteSource.kt)
instance. They must invoke `buildRoutes` on this source, passing in an [`HttpEngine`](#wrapping-it-up-into-an-httpengine).

The implementation of `HttpRouteSource` will have a list of `HttpRouteDefinition`s, which correspond to all of the routes
published by the UAPI. When `buildRoutes` is invoked, these are mapped to instances of `HttpRoute`, with framework-specific
bits added in.

The framework integration is then responsible for mapping the `HttpRoute` instances into their own router, generally
by invoking the appropriate methods on the router to add a route and passing in a framework-specific wrapper around the
`HttpRoute` instance (by convention, this is called a `RouteAdapter`).

The `HttpRouteSource` is obtained by calling (TODO: add the call once it's been defined).

## Request Flow

When a request is received, the HTTP framework will perform its routing and pass the request to the `RouteAdapter`.
This will then forward the request to the proper `HttpRoute`. The `HttpRoute` will call a method on a `Controller` instance
corresponding to the part of the UAPI Model that matches the request path.  This will delegate to the proper `UseCase`
in the core UAPI implementation, which will make the appropriate calls on the developer-supplied code.

Once the `UseCase` finishes running, the UAPI response will be assembled and transformed into the appropriate response 
type, usually JSON, in the `Controller`. The controller will then return an instance of `HttpResponse` to the `RouteAdapter`,
which renders the response in a framework-specific way.

The `RouteAdapter` should usually have an instance of `HttpErrorMapper`, and passes any errors it encounters to the mapper.
It takes the result of the error mapping and sends it to the client.

# Adding new HTTP engines

Each engine needs to define four things: a framework-specific entrypoint, a 'RequestReader' to extract data from the 
framework's request data structure, a Path Formatter for building framework-compatible paths with variables, and some way
to map the abstract HTTP Routes, provided by the `common` package, into routes that the framework can understand.

To get started, create a new directory in this module, named after the framework you want. You should also define a pom.xml
and the basic layout of a Kotlin project. The easiest way to do this is to copy another framework's directory and change
the values in the POM.

Make sure that the POM has a compile dependency on the `common` module, and has a test-scoped dependency on `common-test`.

## The Entrypoint

The entrypoint is going to be different between each framework.  The entrypoint should feel at home in normal usage of
the framework, and should take advantage of as many of the framework's paradigms as possible.

For example, in Spark, you can create a Service (using Service.ignite()), which represents a running HTTP Server:

```kotlin
fun main() {
    val spark = Service.ignite()
    spark.port(8080)
    spark.get("/") { req, resp -> "Hello, World!" }
    spark.path("/foo") {
        spark.get("") { req, resp -> "Hello, Foo!" }
    }
}
```

The UAPI binding for this looks like:

```kotlin
fun main() {
    val uapiModel = buildUApiModel()
    
    val spark = Service.ignite()
    spark.port(8080)
    spark.get("/") { req, resp -> "Hello, World!" }
    spark.uapi(uapiModel)
}
```

Because this integrates idiomatically into the Spark service, you can also take advantage of spark-isms, like nested path groups:

```kotlin
fun main() {
    val uapiV1 = buildV1Model()
    val uapiV2 = buildV2Model()

    val spark = Service.ignite()
    spark.port(8080)
    spark.get("/") { req, resp -> "Hello, World!" }
    spark.path("/v1") {
        spark.uapi(uapiV1)
    }
    spark.path("/v2") {
        spark.uapi(uapiV2)
    }
}
```

Similarly, in Ktor:

```kotlin
fun main() {
    val uapiV1 = buildV1Model()
    val uapiV2 = buildV2Model()

    embeddedServer(Netty) {
        routing {
            get {
              call.respondText("Hello, World!")
            }
            route("v1") {
              uapi(uapiV1)
            }
            route("v2") {
              uapi(uapiV2)
            }
        }
    }
}
```

Each integration feels like an extension of the framework itself.

The `uapi` methods given here accept an [`HttpRouteSource`](common/src/main/kotlin/engines/HttpRouteSource.kt) object.

## RequestReader

The request reader is used to map the specific framework's HTTP Request object to the UAPI's internal request objects, 
without the framework needing to know anything about the UAPI's request types. It defines several methods, each of
which accepts an individual request and returns a value. You can see the specific methods defined in the interface
[here](common/src/main/kotlin/engines/RequestReader.kt).

To help make sure that the request reader implementations behave as expected, there is a 'Contract' test class defined
in the `common-test` package.  You should add a test class that implements the 
[`RequestReaderContractTests`](common-test/src/main/kotlin/edu/byu/uapi/server/http/test/RequestReaderContractTests.kt)
interface.  This defines `val reader`, where you should put your reader instance, and 
`fun buildRequest`, which should construct requests of the type your framework uses. The parent interface
then defines a series of test cases.  If they all pass, your reader is complete!

## Path Formatter

[PathFormatter](common/src/main/kotlin/path/PathFormatter.kt) defines how to take a list of path parts 
(static, single variable, or compound variable) and turn them into a path string that the HTTP framework understands.
Each framework may define different ways of formatting path variables - Spark uses colons (`/foo/:bar`), Ktor uses braces
(`/foo/{bar}`), etc.


There are some pre-defined formatting styles in [PathFormatters](common/src/main/kotlin/path/PathFormatters.kt):

* `COLON` (spark-like)
* `CURLY_BRACE` (ktor-like)

In addition, some frameworks may have issues with the UAPI-style compound path parameters, which are part of the
same path segment, but separated by commas, like `/{foo},{bar},{baz}`.  For handling those, there are versions of
each path formatter defined that can flatten these compound variables:

* `FLAT_COLON` (what Spark actually uses)
* `FLAT_CURLY_BRACE` (what Ktor actually uses)

If you need to define your own path formatter, start with either `SimplePathFormatter` or 
`CompoundFlatteningFormatter` (if you need to flatten compound variables).  Both accept a prefix string and an optional
suffix stream. So, if the HTTP framework I'm working with prefixes variable names with `$` and suffixes them with `!`,
I could use a SimplePathFormatter (`val pathFormatter = SimplePathFormatter("$", "!")`) or
a CompoundFlatteningFormatter (`val pathFormatter = CompoundFlatteningFormatter("$", "!")`).

## Wrapping it up into an HttpEngine

In order to get a list of routes from `HttpRouteSource`, we need to wrap some of this up into an instance of
`HttpEngine`. This can usually be an `object` type, as the values shouldn't usually need to change.


An HTTP engine has a static name (used more for debugging purposes than anything else), a `RequestReader`, and a
`PathFormatter`.  This tells the common UAPI code how to read requests from your framework and how to format paths so
that your framework can understand them.

## Applying Routes

Once you have defined all of the other pieces, we need to actually map the UAPI routes onto your framework's routing engine.

You can get a list of routes you need to map by calling [HttpRouteSource.buildRoutes()](common/src/main/kotlin/engines/HttpRouteSource.kt).
You must pass your HttpEngine instance to this method. This will return a list of 
[HttpRoute](common/src/main/kotlin/engines/HttpRoute.kt), which you can map to your own framework.

Each route has a method, a path (already formatted using your `PathFormatter`), an optional 'consumes' type, 
an optional 'produces' type, and a 'dispatch' method.

You should use the method, path, and content types to route requests to the right handler. Once a match is found,
you should invoke the `dispatch` method, passing the incoming request object.  You must then map the returned
[`HttpResponse`](common/src/main/kotlin/engines/HttpResponse.kt) instance to your framework's response type.

In general, each of your individual route handlers should also wrap their invocation of `dispatch`, and any other parts
of the call that may throw exceptions, in a try-catch block that passes the caught error to the 
[HttpErrorMapper](common/src/main/kotlin/errors/HttpErrorMapper.kt) that is provided in `HttpRouteSource.buildErrorMapper()`.
This will map any uncaught errors to a UAPI-style error body, which is returned to your code as an `HttpResponse`.

As a convenience, you can call `HttpErrorMapper.runHandlingErrors`, which will add the try-catch for you. If the
block provided to `runHandlingErrors` returns successfully, its response will be returned. If not, the response from
the error mapper will be returned.

## Testing your implementation

Once everything is built, you can test that your implementation does everything it is supposed to using the suite of
compliance tests in the `common-test` package.  Just create a test class in `src/integration-tests` which extends
from [`HttpViewComplianceTests`](common-test/src/main/kotlin/edu/byu/uapi/server/http/integrationtest/HttpViewComplianceTests.kt).

You'll have to define two methods - one to start a server on the given address and port which has the given routes,
and one to stop the server.  Then, run the suite, and see what fails!

