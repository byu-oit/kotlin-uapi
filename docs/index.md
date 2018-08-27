# BYU Kotlin UAPI Runtime

This set of libraries provides tools for easily building APIs in Kotlin which adhere to the University API Specification.

# Getting Started

## Dependencies

Add the following to your pom.xml:

```xml
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>edu.byu.uapi.kotlin</groupId>
        <artifactId>uapi-bom</artifactId>
        <version>{insert current version here}</version>
        <scope>import</scope>
        <type>pom</type>
      </dependency>
    </dependencies>
  </dependencyManagement>
  
  <dependencies>
    <dependency>
      <groupId>edu.byu.uapi.kotlin</groupId>
      <artifactId>uapi-runtime</artifactId>
    </dependency>
  </dependencies>
```

If you want to run an HTTP server using Spark, add the `uapi-spark-server` artifact:

```xml
  <dependency>
    <groupId>edu.byu.uapi.kotlin.http</groupId>
    <artifactId>uapi-spark-server</artifactId>
  </dependency>
```

If you want to output an OpenAPI 3 specification for your API, add this artifact:

```xml
  <dependency>
    <groupId>edu.byu.uapi.kotlin.documentation</groupId>
    <artifactId>uapi-openapi3-documentation</artifactId>
  </dependency>
```

## Choose a setup style

There are two styles offered for setting up your API: Interface or DSL. These approaches can be mixed-and-matched.

If you choose the DSL approach, you will need an additional module:

```xml
  <dependency>
    <groupId>edu.byu.uapi.kotlin</groupId>
    <artifactId>kotlin-uapi-dsl</artifactId>
  </dependency>
```

See [Styles](styles.md) for more information.

