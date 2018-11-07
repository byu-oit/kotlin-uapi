---
title: Listing Resources
order: 6
tutorial_source: https://github.com/byu-oit/kotlin-uapi/tree/master/examples/library/tutorial-steps/6-listing-resources
---

# Contents
{:.no_toc}

* This will become the Table of Contents
{:toc}

# Introduction

Most of the time, top-level resources should have a way to get a list of items. This list is accessible as
a GET on the root resource path - so, for the Books resource, you can get a list by calling `GET /books`.

A list endpoint can optionally support various ways of controlling what is returned in the response:

* [Filtering by exact property values](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#60-filters)
* [Sorting the results](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#334-sorted-collections)
* [Getting a subset of the results (AKA "Paging")](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#335-large-collections)
* [Full-text search](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#70-search)

# Basic Lists

Let's start off with a simple list, which contains all books in an unspecified order.

The first part of implementing any list functionality on a resource is to implement the `IdentifiedResource.Listable` interface:

```diff
- class BooksResource : IdentifiedResource<LibraryUser, Long, Book> {
+ class BooksResource : IdentifiedResource<LibraryUser, Long, Book>,
+                       IdentifiedResource.Listable<LibraryUser, Long, Book, ListParams.Empty> {
```

The first three generic parameters must match those on the resource. It would be nice if we could omit them,
but unfortunately, Generics just don't quite work that way.

The fourth generic parameter specifies a class which will hold all of the parameters our list endpoint supports.
For now, we'll use `ListParams.Empty`, which is a special type that tells the Runtime that we don't expect any
parameters.

As a convenience, if you want to implement a list endpoint with no parameters, you can swap the `Listable`
interface in our example with `Listable.Simple`:

```diff
class BooksResource : IdentifiedResource<LibraryUser, Long, Book>,
-                       IdentifiedResource.Listable<LibraryUser, Long, Book, ListParams.Empty> {
+                       IdentifiedResource.Listable.Simple<LibraryUser, Long, Book> {
```

These two declarations are functionally equivalent, though the `Simple` version may have a slight performance
edge when first starting the application.

Now, you should be seeing some compiler errors in your IDE. We need to implement the `list` method:

```diff
class BooksResource : IdentifiedResource<LibraryUser, Long, Book>,
                      IdentifiedResource.Listable.Simple<LibraryUser, Long, Book> {
+  override fun list(
+    userContext: LibraryUser,
+    params: ListParams.Empty
+  ): List<Book> {
+    return Library.listAllBooks()
+  }

```

This list takes in a user context and an instance of the parameter holder we specified. It returns a list
of our model type.

{% capture list_must_enforce_user %}
You **MUST** enforce a user's authorizations before returning a result from `list`. In other words, the returned
list must only contain entries the user is authorized to see.
{% endcapture %}
{% include callouts/warning.html content=list_must_enforce_user %}
