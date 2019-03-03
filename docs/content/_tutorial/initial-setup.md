---
title: Initial Setup
order: 1
tutorial_source: https://github.com/byu-oit/kotlin-uapi/tree/master/examples/library/tutorial-steps/1-initial-setup
---

TODO: Create maven archetypes for UAPI projects - an empty one, and one with demo code

Create a new Kotlin-JVM project. It is easiest to use a Maven Archetype, so that you don't
have to remember how to do so:

```bash
mvn archetype:generate -DarchetypeGroupId=org.jetbrains.kotlin -DarchetypeArtifactId=kotlin-archetype-jvm
```

You will be prompted to enter several values. This tutorial will assume that you entered the following values:

Prompt | Value
-------|-------
groupId | edu.byu.uapi.library
artifactId | library-tutorial-api
version | (use default)
package | (use default)

Follow the prompts, and you will end up with a basic Kotlin-JVM project.

Now, add the required dependencies from the [Maven Artifacts](../_reference/maven-artifacts.md) page to your `pom.xml`.
You will need the core runtime and the Spark HTTP adapter.

You'll also want to add the pre-written backend for the application, which contains an in-memory database
and a working application layer, but no API layer. Note that you will need to include the version number here,
as the UAPI Bill of Materials POM does not include the example code.

```xml
<dependency>
  <groupId>edu.byu.uapi.server.examples</groupId>
  <artifactId>library-example-common</artifactId>
  <version>{Latest version here}</version>
</dependency>
```
{% include uapi-version.html %}

We also need to tell the Kotlin compiler to target Java 8. Add the following to the Kotlin maven plugin configuration:

```xml
<configuration>
    <jvmTarget>1.8</jvmTarget>
</configuration>
```

It's pretty useful to be able to get debug logs, right? The Runtime and its dependencies use the pluggable
SLF4J facade for logging; you need to add a dependency to actually make it log things. We keep a 
[partial list](../_reference/maven-artifacts.md#logging) of compatible logging tools. Normally you'd
use something fully-featured, like Log4J, but we'll just use the simple SLF4J logger, which dumps logs
to stdout:

```xml
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-simple</artifactId>
  <version>{latest SLF4j Version}</version>
</dependency>
```

{% include maven-version.html group="org.slf4j" artifact="slf4j-simple" ver_filter="1.7" %}

You can feel free to remove the `junit` and `kotlin-test` dependencies,
as well as `src/main/kotlin/edu/byu/uapi/library/Hello.kt` and 
`src/test/kotlin/edu/byu/uapi/library/HelloTest.kt`.

Now you're ready to start coding!


