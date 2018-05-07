# kotlin-uapi-dsl

**This is very, very much a work in progress and could break or change at any time**

This collection of modules provides a type-safe Kotlin DSL for designing and implementing RESTful Web Services that conform to the
[University API Standard](https://github.com/byu-oit/UAPI-Specification).

# Status

This project is under active development and can't actually do anything yet.

Milestones:

- [ ] Run basic service on top of Spark
- [ ] Automatic link handling
- [ ] Automatic field-level metadata manipulation
- [ ] Automatic subresource/fieldset handling
- [ ] Type-safe collection querying & paging
- [ ] Output OpenAPI 2 and 3 documents
- [ ] Automatic [Meta APIs](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#80-meta-data-sets-and-apis)
- [ ] Customizable type serialization
- [ ] Attempt to support running on Node.js with Express or something similar

# Packages

See the individual packages for documentation.

## Core DSL (`dsl/`)

Contains the main DSL definition language.

## UAPI Datatypes (`uapi-types/`)

Contains data structures that are common between server and client implementations, including pre-configured serializers
for Jackson.

## Adapters (`adapters/`)

Contains various adapters that transform the output of the core DSL. An adapter may do anything from outputting a definition
of the service in a service definition language, like OpenAPI or RAML, to instantiating a full-fledged web server that 
runs the service.

### Open API 3 (`adapters/openapi3`)

Outputs an OpenAPI 3 (Swagger 3) version of the service definition.

### Spark

Runs the service on top of the [Spark Web framework](sparkjava.com).


## Examples (`examples/`)

Various examples of using the DSL. All examples are self-contained and operate only on hardcoded data.

### Persons-JVM (`examples/persons-jvm`)

A JVM-based subset of the official Persons V2 BYUAPI.

