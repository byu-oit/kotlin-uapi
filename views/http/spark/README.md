# UAPI HTTP - Spark Edition

This package binds a UAPI Model to HTTP using [Spark](http://sparkjava.com).

Start by adding the appropriate package to your POM:

```xml
<dependency>
  <groupId>edu.byu.uapi.server.http</groupId>
  <artifactId>uapi-http-spark</artifactId>
  <version>latest version here</version>
</dependency>
```

![Latest Version](https://img.shields.io/github/release-pre/byu-oit/kotlin-uapi.svg?style=for-the-badge&label=Latest+UAPI+Release&colorA=002E5D&colorB=5F7C9B&logoWidth=30&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMzkuNyA0MC4zIj48ZyBmaWxsPSIjRkZGIj48cGF0aCBkPSJNMTYuNiAxNi4xVjkuNGMwLS41LS4xLS45LS4xLS45cy41LjEuOS4xaDVjMy44IDAgNi4xLjYgNi4xIDQuMSAwIDIuMy0xLjQgNC4zLTUuOSA0LjNoLTZ2LS45em0wIDE1LjN2LTcuN2MwLS41LS4xLS45LS4xLS45cy41LjEuOS4xaDQuM2MzLjQgMCA3LjUgMCA3LjUgNC43IDAgMy41LTMgNC43LTcgNC43aC00LjhjLS41IDAtLjkuMS0uOS4xcy4xLS41LjEtMXpNNC40IDQuNXYzMi4xYzAgMSAuMSAxLjQtLjcgMS45LS43LjQtMS40LjUtMi42LjctLjEgMC0uMS41IDAgLjVoMjZjMTEgMCAxNS42LTQuMyAxNS42LTExLjEgMC01LTIuMy04LjEtNy05LjItLjEgMC0uMS0uMSAwLS4xIDIuOS0uOSA1LjctMy4xIDUuNy04LjMgMC03LjEtNC44LTkuNS0xNS4yLTkuNWgtMjVjLS4yLS4xLS4yLjMtLjEuNCAxLjIuMiAxLjkuMyAyLjYuNy44LjUuNy44LjcgMS45ek0xMzUuMyA0LjVjMC0xLS4xLTEuNC43LTEuOS43LS40IDEuNS0uNSAyLjctLjcuMSAwIC4xLS41IDAtLjVoLTE5LjJjLS4xIDAtLjEuNCAwIC41IDEuMi4yIDEuOC4zIDIuNS43LjcuNS43LjguNyAxLjl2MTkuM2MwIDQuNC0yLjUgNy44LTcuNSA3LjhzLTcuNS0zLjQtNy41LTcuOFY0LjVjMC0xLS4xLTEuNC43LTEuOS43LS40IDEuMy0uNiAyLjUtLjcuMSAwIC4xLS41IDAtLjVIOTIuOGMtLjEgMC0uMS41IDAgLjUuNyAwIDEuMS4xIDEuNS40LjYuNS43IDEuMS43IDIuMnYxOWMwIDkuMSA1IDE2LjkgMjAuMiAxNi45czIwLjItNy44IDIwLjItMTYuOXYtMTl6TTc3LjMgMzkuN2MuMSAwIC4xLS40IDAtLjUtMS4yLS4yLTEuOS0uMy0yLjctLjctLjctLjUtLjctLjgtLjctMS45VjI0LjRMODcuMiA0LjdjLjctMS4xIDEuMi0xLjYgMS44LTIuMS4xLS4xLjMtLjIuNS0uMy41LS4zLjctLjQgMS40LS40LjEgMCAuMi0uNSAwLS41SDczYy0uMSAwLS4xLjUgMCAuNS45IDAgMS45IDAgMS44IDEuMy0uMSAxLjItNS4zIDguNy03LjEgMTEuNS0uMy40LS41LjgtLjYgMS4zLS4xLS41LS40LTEtLjYtMS4zLTIuNi0zLjgtNi45LTEwLTcuMS0xMS41LS4xLTEuMy45LTEuMyAxLjgtMS4zLjEgMCAuMi0uNSAwLS41SDQyLjRjLS4xIDAtLjEuNCAwIC41LjguMSAxLjIgMCAyLjIuNS4xLjEuNC4yLjUuMy42LjUgMSAxIDEuNyAyTDYwIDI0LjN2MTIuM2MwIDEgLjEgMS40LS43IDEuOS0uNy40LTEuNS41LTIuNy43LS4xIDAtLjEuNSAwIC41aDIwLjd6Ii8+PC9nPjwvc3ZnPg==)

You can use this package with either the static Spark API, or the spark Service API (using `Service.ignite()`). You can
map any other routes and add before/after/afterAfter filters using normal Spark APIs.

## Static Spark API

### Root Path

```kotlin
fun main() {
    val uapiModel = //get UAPI Model
  
    port(8080)
    
    addUApiToSpark(uapiModel)
}
```

### Sub-path

```kotlin
fun main() {
    val uapiModel = //get UAPI Model
  
    port(8080)
    
    path("v1") {
        addUApiToSpark(uapiModel)
    }
}
```

## Spark Service API

### Root Path

```kotlin
fun main() {
    val uapiModel = //get UAPI Model
    
    val spark = Service.ignite()
  
    spark.port(8080)
    
    spark.uapi(uapiModel)
}
```

### Sub-path

```kotlin
fun main() {
    val uapiModel = //get UAPI Model
  
    val spark = Service.ignite()
  
    spark.port(8080)
    
    spark.path("v1") {
        spark.uapi(uapiModel)
    }
}
```
