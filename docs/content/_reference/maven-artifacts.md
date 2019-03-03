---
title: Maven Artifacts
order: -1
---

# Maven Artifacts

All artifacts are available in the BYU Maven repository.

The UAPI runtime requires Java 8 or later.

## Bill of Materials

The easiest way to keep all of the artifacts used by the UAPI Runtime in sync is to
use the Bill of Materials POM. This allows you to set one version number and not have to 
set it anywhere else.

To import a Bill of Materials, you must add a `<dependencyManagement>` section to your `pom.xml`:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>edu.byu.uapi.server</groupId>
      <artifactId>uapi-bom</artifactId>
      <version>{insert latest version here}</version>
      <scope>import</scope>
      <type>pom</type>
    </dependency>
  </dependencies>
</dependencyManagement>
```
{% include uapi-version.html %}

## Packages

Assuming you have imported the Bill of Materials POM, you do not need to specify version numbers for
any of the individual packages.

All projects will require the core runtime artifact:

```xml
<dependencies>
  <!-- other dependencies -->
  <dependency>
    <groupId>edu.byu.uapi.server</groupId>
    <artifactId>uapi-runtime</artifactId>
  </dependency>
</dependencies>
```

## Running an HTTP Server

### [Spark](http://sparkjava.com)

```xml
<dependencies>
  <!-- other dependencies -->
  <dependency>
    <groupId>edu.byu.uapi.server.http</groupId>
    <artifactId>spark-http-adapter</artifactId>
  </dependency>
</dependencies>
```

### [Ktor](https://ktor.io/)

TODO

### Traditional Servlet (Tomcat, etc)

TODO

## Running in AWS Lambda

TODO

## Testing Tools

TODO

## Documentation Tools

TODO

# Logging

The UAPI Runtime uses [SLF4j 1](https://www.slf4j.org/) for logging. To view logs from the Runtime,
you must include a bridge to the logging framework of your choice.

## Logging to the console

To simply dump all logs to the console, use slf4j-simple. This is mostly suitable for using during
automated testing; production applications should have more advanced logging.

```xml
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-simple</artifactId>
  <version>{latest SLF4j Version}</version>
</dependency>
```

{% include maven-version.html group='org.slf4j' artifact='slf4j-simple' ver_filter='1.7' %}


## [Log4j 2](https://logging.apache.org/log4j/2.x/)

If you haven't heard of Log4j, you haven't been around the Java world for long.

You're on your own for figuring out the details of your Log4j logger configuration.

```xml
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-slf4j-impl</artifactId>
    <version>{latest log4j 2 version}</version>
</dependency>
```

{% include maven-version.html group='org.apache.logging.log4j' artifact='log4j-slf4j-impl' %}

## [Tinylog](https://tinylog.org/)

Tinylog is a very small and fast logger, ideal for size-sensitive environments like AWS Lambda and Android. 
It offers bindings for SLF4j.

```xml
<dependency>
  <groupId>org.tinylog</groupId>
  <artifactId>tinylog</artifactId>
  <version>{latest tinylog version}</version>
</dependency>
<dependency>
  <groupId>org.tinylog</groupId>
  <artifactId>slf4j-binding</artifactId>
  <version>{latest tinylog version}</version>
</dependency>
```

{% include maven-version.html group='org.tinylog' artifact='tinylog' %}
{% include maven-version.html group='org.tinylog' artifact='slf4j-binding' %}
