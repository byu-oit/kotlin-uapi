[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=byu-oit/kotlin-uapi)](https://dependabot.com)

# kotlin-uapi-dsl

[Start by reading the docs](https://byu-oit.github.io/kotlin-uapi). The tutorials are slightly out-of-date (yaaay tight project deadlines!), but will at least get you pointed in the right direction.

**This is very, very much a work in progress and could break or change at any time**

This collection of modules provides a type-safe Kotlin runtime for designing and implementing RESTful Web Services that conform to the
[University API Standard](https://github.com/byu-oit/UAPI-Specification).

# Status

The latest release line is 0.5.x.  This provides a working subset of UAPI features.

Now that we understand the problem domain better, and since Kotlin has evolved in the meantime, the upcoming 0.6.x release line will be a substantial re-write to address the issues we've discovered using the MVP in real applications. The general principals, especially concerning the developer experience, will remain the same.

The goals of the 0.6.x release line:

- COROUTINE ALL THE THINGS!!!
- Add a simple, hook-able lifecycle to requests and configuration
- Rewrite the entire request pipeline to be simpler and more consistent
- Break things into small, testable chunks (and, you know, actually test them)
- Lay the groundwork for supporting newer parts of the UAPI spec, like filter operators, binary endpoints, field-level authorization, subresource filtering, and whatever else gets dreamed up in Architect's Roundtable each week.
- Be more opinionated about certain things, like ceasing to abstract JSON processing and just use Jackson. Since the BYU Apps team intends to constantly be patching applications to our latest tooling choices, there's not a big reason to keep things like JSON processing swappable - we'll just cut a new major version of the runtime and upgrade all of our APIs to it over the following weeks

Milestones:

- [x] Fetch a single resource
- [x] Field metadata generation
- [x] Fetch a list of resources w/ typesafe parameters
  * [x] Validate filter params
  * [ ] Authorize filter params
- [x] Run HTTP on top of Spark
- [x] Resource Creation
- [x] Resource Modification
- [x] Resource Deletion
- [ ] Automatic link handling
- [x] Automatic subresource/fieldset handling
- [ ] Run HTTP on top of ktor
- [ ] Use Coroutines when possible
- [x] Output OpenAPI 2 and 3 documents
- [x] Customize name of simple path IDs
- [ ] Automatic [Meta APIs](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#80-meta-data-sets-and-apis)
- [ ] Automatic related resource linking

# Joseph's Big List of Crazy Ideas

* Standard Field Definitions (byu_id, etc.)
* Lifecycle hooks
* Make all functions `suspend` and use coroutines
* Extension modules - use lifecycle hooks to do things like add caching, etc.
