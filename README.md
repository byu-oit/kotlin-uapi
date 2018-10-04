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

# Development

## Important Concepts For Developers

### API Model

The API model is the output of the DSL. It contains a data structure representing all of the resources, subresources, relationships, etc., including type mappings, field names and types, etc.  It is a complete specification of the API, and contains all information needed to implement or consume the API.

### UAPIScalar

Subclasses of the sealed class UAPIScalar represent the different datatypes that are natively understood by the UAPI.  These are the basic JSON types, plus types which are serialized to a string, but are governed by a non-JSON specification. For example, a date/time is serialized as a string, but the format of that string is governed by [RFC 3339](https://tools.ietf.org/html/rfc3339), so there is a UAPIDateTime scalar class which understands this serialization.

All data types that are serialized to and from our UAPI implementations must be converted to/from one of these scalar types. The datatypes module contains JSON serialization/deserialization providers for all of the scalar types.

# Joseph's Big List of Crazy Ideas

* Standard Field Definitions (byu_id, etc.)
* Lifecycle hooks
* Make all functions `suspend` and use coroutines
* Extension modules - use lifecycle hooks to do things like add caching, etc.
