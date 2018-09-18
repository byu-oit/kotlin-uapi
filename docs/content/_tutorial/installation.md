---
title: Installation
order: 1
---

# Installation

All artifacts are available in the BYU Maven repository.

# Requirements

The UAPI runtime requires Java 8 or later.

# Artifacts

## Bill of Materials

The easiest way to keep all of the artifacts used by the UAPI Runtime in sync is to
use the Bill of Materials POM. This allows you to set one version number and not have to 
set it anywhere else.

To import a Bill of Materials, you must add a `<dependencyManagement>` section to your `pom.xml`:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>edu.byu.uapi.kotlin</groupId>
      <artifactId>uapi-bom</artifactId>
      <version>{insert latest version here}</version>
      <scope>import</scope>
      <type>pom</type>
    </dependency>
  </dependencies>
</dependencyManagement>
```

## Packages

Assuming you have imported the Bill of Materials POM, you do not need to specify version numbers for
any of the individual packages.

All projects will require the core runtime artifact:

```xml
<dependencies>
  <!-- other dependencies -->
  <dependency>
    <groupId>edu.byu.uapi.kotlin</groupId>
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
    <groupId>edu.byu.uapi.kotlin.http</groupId>
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
