---
title: Initial Setup
order: 1
---

# Initial Setup

TODO: Create maven archetypes for UAPI projects - an empty one, and one with demo code

Create a new Kotlin-JVM project. It is easiest to use a Maven Archetype, so that you don't
have to remember how to do so:

```
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
  <groupId>edu.byu.uapi.kotlin.examples</groupId>
  <artifactId>library-example-common</artifactId>
  <version>{Latest version here}</version>
</dependency>
```

Now you're ready to start coding!
