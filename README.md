[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=byu-oit/kotlin-uapi)](https://dependabot.com)

# kotlin-uapi-dsl

**This is very, very much a work in progress and could break or change at any time**

This collection of modules provides a type-safe Kotlin runtime for designing and implementing RESTful Web Services that conform to the
[University API Standard](https://github.com/byu-oit/UAPI-Specification).

# Status

This project is under active development and can't actually do anything yet.

Milestones:

- [x] Fetch a single resource
- [x] Field metadata generation
- [x] Fetch a list of resources w/ typesafe parameters
  * [ ] Validate filter params
  * [ ] Authorize filter params
- [x] Run HTTP on top of Spark
- [x] Resource Creation
- [x] Resource Modification
- [x] Resource Deletion
- [ ] Automatic link handling
- [ ] Automatic subresource/fieldset handling
- [ ] Run HTTP on top of ktor
- [ ] Use Coroutines when possible
- [ ] Output OpenAPI 2 and 3 documents
- [ ] Automatic [Meta APIs](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#80-meta-data-sets-and-apis)
- [ ] Automatic related resource linking

# Usage

Check out the [project site](https://byu-oit.github.io/kotlin-uapi/).

# Joseph's Big List of Crazy Ideas

* Standard Field Definitions (byu_id, etc.)
* Lifecycle hooks
* Make all functions `suspend` and use coroutines
* Extension modules - use lifecycle hooks to do things like add caching, etc.
