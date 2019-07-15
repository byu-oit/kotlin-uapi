[![AWS CodebuildBuild Status](https://codebuild.us-west-2.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoiY01FaWZPSDMvdWd2V2U3U21JcERGd1FHdEF0ZDRLcDMvejBOLzNGY000OEN6eUVXZTFWdE9NUHluL01rb0ZFdUhIWTlkQzlqVkFXeFZCY25HTjVVa1dNPSIsIml2UGFyYW1ldGVyU3BlYyI6Imc0SVhsbUV4SjNpZGZHVnQiLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=develop)](https://us-west-2.console.aws.amazon.com/codesuite/codebuild/projects/kotlin-uapi/)
[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=byu-oit/kotlin-uapi)](https://dependabot.com)
![GitHub release](https://img.shields.io/github/release-pre/byu-oit/kotlin-uapi.svg?style=for-the-badge&label=Latest+UAPI+Release&colorA=002E5D&colorB=5F7C9B&logoWidth=30&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMzkuNyA0MC4zIj48ZyBmaWxsPSIjRkZGIj48cGF0aCBkPSJNMTYuNiAxNi4xVjkuNGMwLS41LS4xLS45LS4xLS45cy41LjEuOS4xaDVjMy44IDAgNi4xLjYgNi4xIDQuMSAwIDIuMy0xLjQgNC4zLTUuOSA0LjNoLTZ2LS45em0wIDE1LjN2LTcuN2MwLS41LS4xLS45LS4xLS45cy41LjEuOS4xaDQuM2MzLjQgMCA3LjUgMCA3LjUgNC43IDAgMy41LTMgNC43LTcgNC43aC00LjhjLS41IDAtLjkuMS0uOS4xcy4xLS41LjEtMXpNNC40IDQuNXYzMi4xYzAgMSAuMSAxLjQtLjcgMS45LS43LjQtMS40LjUtMi42LjctLjEgMC0uMS41IDAgLjVoMjZjMTEgMCAxNS42LTQuMyAxNS42LTExLjEgMC01LTIuMy04LjEtNy05LjItLjEgMC0uMS0uMSAwLS4xIDIuOS0uOSA1LjctMy4xIDUuNy04LjMgMC03LjEtNC44LTkuNS0xNS4yLTkuNWgtMjVjLS4yLS4xLS4yLjMtLjEuNCAxLjIuMiAxLjkuMyAyLjYuNy44LjUuNy44LjcgMS45ek0xMzUuMyA0LjVjMC0xLS4xLTEuNC43LTEuOS43LS40IDEuNS0uNSAyLjctLjcuMSAwIC4xLS41IDAtLjVoLTE5LjJjLS4xIDAtLjEuNCAwIC41IDEuMi4yIDEuOC4zIDIuNS43LjcuNS43LjguNyAxLjl2MTkuM2MwIDQuNC0yLjUgNy44LTcuNSA3LjhzLTcuNS0zLjQtNy41LTcuOFY0LjVjMC0xLS4xLTEuNC43LTEuOS43LS40IDEuMy0uNiAyLjUtLjcuMSAwIC4xLS41IDAtLjVIOTIuOGMtLjEgMC0uMS41IDAgLjUuNyAwIDEuMS4xIDEuNS40LjYuNS43IDEuMS43IDIuMnYxOWMwIDkuMSA1IDE2LjkgMjAuMiAxNi45czIwLjItNy44IDIwLjItMTYuOXYtMTl6TTc3LjMgMzkuN2MuMSAwIC4xLS40IDAtLjUtMS4yLS4yLTEuOS0uMy0yLjctLjctLjctLjUtLjctLjgtLjctMS45VjI0LjRMODcuMiA0LjdjLjctMS4xIDEuMi0xLjYgMS44LTIuMS4xLS4xLjMtLjIuNS0uMy41LS4zLjctLjQgMS40LS40LjEgMCAuMi0uNSAwLS41SDczYy0uMSAwLS4xLjUgMCAuNS45IDAgMS45IDAgMS44IDEuMy0uMSAxLjItNS4zIDguNy03LjEgMTEuNS0uMy40LS41LjgtLjYgMS4zLS4xLS41LS40LTEtLjYtMS4zLTIuNi0zLjgtNi45LTEwLTcuMS0xMS41LS4xLTEuMy45LTEuMyAxLjgtMS4zLjEgMCAuMi0uNSAwLS41SDQyLjRjLS4xIDAtLjEuNCAwIC41LjguMSAxLjIgMCAyLjIuNS4xLjEuNC4yLjUuMy42LjUgMSAxIDEuNyAyTDYwIDI0LjN2MTIuM2MwIDEgLjEgMS40LS43IDEuOS0uNy40LTEuNS41LTIuNy43LS4xIDAtLjEuNSAwIC41aDIwLjd6Ii8+PC9nPjwvc3ZnPg==)

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
