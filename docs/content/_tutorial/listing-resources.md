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

* [Getting a subset of the results (AKA "Paging")](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#335-large-collections)
* [Sorting the results](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#334-sorted-collections)
* [Filtering by property values](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#60-filters)
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
+    val result = Library.listBooks(
+      includeRestricted = userContext.canViewRestrictedBooks
+    )
+    return result.list
+  }
```

> `includeRestricted = userContext.canViewRestrictedBooks` is a Kotlin *named parameter*.  Named parameters are
> Very Useful, and you'll be seeing more of them in this tutorial.
{: .callout-kotlin }

This list takes in a user context and an instance of the parameter holder we specified. It returns a class that contains
the list and the total size of the matched collection (we'll see why that's important later).

> You **MUST** enforce a user's authorizations before returning a result from `list`. In other words, the returned
> list must only contain entries the user is authorized to see.
> 
> The Runtime will try to catch errors in handling this by checking that the user is authorized to view each row
> and throwing an exception if they are not, but this leaks information to an attacker about items they are not allowed
> to view. This check is not performed as an absolute protection against data leakage, but as a least-bad option (as in,
> it's better to throw the exception, which could possibly leak data, than to just return the data).
{: .callout-warning }

Alright! Let's see what happens when we get a list!

```bash
curl -H "Authorization: Bearer {your OAuth token here}" http://localhost:8080/books
```

Oh My! That gave us a lot of data! (it should be 1000+ lines of JSON).

```json
{
    "values": [
        {
            "basic": {
                "oclc": {
                    "value": 733291011,
                    "api_type": "read-only",
                    "key": true,
                    "display_label": "OCLC Control Number"
                },
                "title": {
                    "value": "The War of the Worlds",
                    "api_type": "modifiable",
                    "display_label": "Title"
                },
                "publisher_id": {
                    "value": 6,
                    "description": "Signet",
                    "long_description": "Signet Books",
                    "api_type": "modifiable",
                    "display_label": "Publisher"
                },
                "available_copies": {
                    "value": 0,
                    "api_type": "derived",
                    "display_label": "Available Copies"
                },
                "isbn": {
                    "value": "978-0451530653",
                    "api_type": "system",
                    "display_label": "ISBN"
                },
                "subtitles": {
                    "values": [],
                    "api_type": "modifiable",
                    "display_label": "Subtitles"
                },
                "author_ids": {
                    "values": [
                        {
                            "value": 1,
                            "description": "H. G. Wells"
                        }
                    ],
                    "api_type": "modifiable",
                    "display_label": "Author(s)"
                },
                "genres": {
                    "values": [
                        {
                            "value": "SFI",
                            "description": "Science Fiction"
                        }
                    ],
                    "api_type": "modifiable",
                    "display_label": "Genre(s)"
                },
                "published_year": {
                    "value": 1898,
                    "api_type": "modifiable",
                    "display_label": "Publication Year"
                },
                "restricted": {
                    "value": false,
                    "api_type": "modifiable",
                    "display_label": "Is Restricted"
                },
                "links": {},
                "metadata": {
                    "validation_response": {
                        "code": 200,
                        "message": "OK"
                    }
                }
            },
            "links": {},
            "metadata": {
                "validation_response": {
                    "code": 200,
                    "message": "OK"
                },
                "field_sets_returned": [
                    "basic"
                ],
                "field_sets_available": [
                    "basic"
                ],
                "field_sets_default": [
                    "basic"
                ],
                "contexts_available": {}
            }
        },
        ... Rest of the results here ...
    ],
    "links": {},
    "metadata": {
        "validation_response": {
            "code": 200,
            "message": "OK"
        },
        "collection_size": 11
    }
}
```

This all matches the UAPI Specification. You can read more about how collections are represented 
[here](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#33-representing-a-collection-of-resources)

You'll notice that there is a new value in the top-level metadata: `collection_size`. This is the total number of items
that match our query; this may not match the actual number of results returned (see ["Subsets" below](#subsets-paging)).

# Adding More List Features

New abilities are added to our list function by implementing extra interfaces. Based on which interfaces are implemented,
the Runtime knows what your resource can do.

The trick to this is that we need to define a *parameter class* that will contain all of the different parameters that can
be passed to our list. The parameter class must be a data class (we'll cover how to work around that rule later), and must
implement one of several interfaces.
  
Let's start out by creating a `BookListParams` class. Let's put in in `BookListParams.kt` in the
same package as your resource class:

```kotlin
data class BookListParams(

)
```

> This won't compile yet. We'll fix that in a moment!

For each list feature we want to add, we must add an interface like `IdentifiedResource.Listable.WithSorting` to our
resource and one like `ListParams.WithSorting` to our parameter class.

Each list feature adds new query parameters for the client to use, and most add new metadata values that will be
included in the response.

## Subsets (Paging)

When we request a list, we get a *lot* of data back, and as our library adds more books, we'll quickly reach the point
where our responses are just too big for a client to handle. So, let's learn how to allow the client to request a 'subset'
of the results. This is often referred to as "paging."

Subsets are implemented using `IdentifiedResource.Listable.WithSubset` and `ListParams.WithSubset`. This adds the following
query parameters and metadata values:

Query Parameter | Type | Description
----------------|------|------------
`subset_start_offset` | integer | The zero-based offset to apply to the subset. Defaults to '0'
`subset_size`     | integer | The maximum number of records to include in the subset.
`subset_start_key` | any | The primary key of the resource to start the subset with. Optional alternative to subset_start_offset.

> `subset_start_key` has not yet been implemented. It will be implemented when someone has a use case for it.
{: .callout-in-progress }

Metadata Value | Description
---------------|------------
`default_subset_size` | The size used if no `subset_size` parameter is passed.
`max_subset_size`     | The largest subset this API will return. Passing a value of `subset_size` which is larger than `max_subset_size` is equivalent to passing `max_subset_size`.
`subset_start`        | The actual offset of the first item in the subset.
`subset_size`         | The actual number of items returned in the subset. This may not match the requested `subset_size`, usually when fetching the last subset in the collection.

Let's add subset support to our list. We do that by making our resource implement `IdentifiedResource.Listable.WithSubset`:

```diff
  class BooksResource : IdentifiedResource<LibraryUser, Long, Book>,
-                       IdentifiedResource.Listable.Simple<LibraryUser, Long, Book> {
+                       IdentifiedResource.Listable.WithSubset<LibraryUser, Long, Book, BookListParams> {
   override fun list(
     userContext: LibraryUser,
-    params: ListParams.Empty
+    params: BookListParams
   ): List<Book> {
     val result = Library.listBooks(
       includeRestricted = userContext.canViewRestrictedBooks
     )
     return result.list
   }
+  
+  override val listDefaultSubsetSize: Int = 50
+  override val listMaxSubsetSize: Int = 100
```

Here, we've specified that our parameter class is `BookListParams`, our default subset size is 50, and we allow
subsets up to 100 items in length.
  
But look! You've got compile errors!  Let's fix them!

The first thing to notice is that, by adding `WithSubset`, the return type of `list` has changed from `List` to
`ListWithTotal`. That's because we need a way for you to tell the runtime what the *total size* of the subset is,
not just the size of the subset we returned.  Luckily, `Library.listBooks` already returns that value to us, so we
can take advantage of that:

```diff
  class BooksResource : IdentifiedResource<LibraryUser, Long, Book>,
                        IdentifiedResource.Listable.WithSubset<LibraryUser, Long, Book, BookListParams> {
   override fun list(
     userContext: LibraryUser,
     params: BookListParams
-  ): List<Book> {
+  ): ListWithTotal<Book> {
     val result = Library.listBooks(
       includeRestricted = userContext.canViewRestrictedBooks
     )
-    return result.list
+    return ListWithTotal(
+     totalItems = result.totalItems,
+     values = result.list
+    )
   }
   
   override val listDefaultSubsetSize: Int = 50
   override val listMaxSubsetSize: Int = 100
```

> Only resources that use `WithSubset` need to return `ListWithTotal`. Every other combination of features just returns
> a `List`.
{: .callout-protip }

Our next compile error is in our declaration of our interface. The compiler is complaining because `BookListParams` doesn't
implement `ListParams.WithSubset`. Let's fix that in `BookListParams.kt`:

```diff
  data class BookListParams(
+     override val subset: SubsetParams
- )
+ ) : ListParams.WithSubset
```

Now that we have a way to get our subset parameters, we can pass them along to our database query:

```diff
  class BooksResource : IdentifiedResource<LibraryUser, Long, Book>,
                        IdentifiedResource.Listable.WithSubset<LibraryUser, Long, Book, BookListParams> {
   override fun list(
     userContext: LibraryUser,
     params: BookListParams
   ): ListWithTotal<Book> {
     val result = Library.listBooks(
-      includeRestricted = userContext.canViewRestrictedBooks
+      includeRestricted = userContext.canViewRestrictedBooks,
+      subsetSize = params.subset.subsetSize,
+      subsetStart = params.subset.subsetStartOffset
     )
     return ListWithTotal(
      totalItems = result.totalItems,
      values = result.list
     )
   }
   
   override val listDefaultSubsetSize: Int = 50
   override val listMaxSubsetSize: Int = 100
```

Now, let's get just the second book in our list:

```bash
curl -H "Authorization: Bearer {your OAuth token here}" http://localhost:8080/books?subset_start_offset=1&subset_size=1
```

> Note that subset_start_offset is zero-based.

```json
{
  "values":[
    {
      "basic":{
        "oclc":{
          "value":35231812,
          "api_type":"read-only",
          "key":true,
          "display_label":"OCLC Control Number"
        },
        "title":{
          "value":"Catch-22",
          "api_type":"modifiable",
          "display_label":"Title"
        },
        // ... we've omitted a bunch of properties, because they're REALLY LONG.
        "links":{

        },
        "metadata":{
          "validation_response":{
            "code":200,
            "message":"OK"
          }
        }
      },
      "links":{

      },
      "metadata":{
        "validation_response":{
          "code":200,
          "message":"OK"
        },
        "field_sets_returned":[
          "basic"
        ],
        "field_sets_available":[
          "basic"
        ],
        "field_sets_default":[
          "basic"
        ],
        "contexts_available":{

        }
      }
    }
  ],
  "links":{

  },
  "metadata":{
    "validation_response":{
      "code":200,
      "message":"OK"
    },
    "collection_size":11,
    "subset_size":1,
    "subset_start":1,
    "default_subset_size":50,
    "max_subset_size":100
  }
}
```

Well, that's a whole lot of data. But it's less than we had before!

Notice that there are some new metadata values. Especially notice that the `subset_size` and `collection_size` are different -
there are 11 books in our database that match our query, but our subset only contains one of them.
 
Let's declare subsetting a success, and move on to more exciting things!

## Sorting

Often, we want to allow the client to specify in which order they want to receive results.  This is especially true when
working with large datasets, where we can't expect the client to keep the entire dataset in memory and sort it there. As
a rule of thumb, if you have a dataset that is large enough to need subset support, you should also support sorting.

Sorting adds the following query parameters and metadata values:

Query Parameter | Type | Description
----------------|------|------------
sort_properties	| comma-separated list | List of parameters to use in sorting.
sort_order | `ascending` or `descending` | The order in which to sort. Only one order can be specified.

Metadata Value | Description
---------------|------------
sort_properties_available | Which properties can be used to sort
sort_properties_default	| Which properties are used if not specified by the client
sort_order_default | What order is used if not specified by the client


`Library.listBooks` accepts two parameters for
doing sorting: `sortColumns` and `sortAscending`. `sortColumns` accepts a list of an enum type called 
`BookSortableColumns`, and `sortAscending` is a boolean where `false` makes the sort go in descending order. 
Let's see how to use these to add sorting to our list view!

In order to make our collection sortable, we first need to define an enum that represents all of the properties that our
collection can be sorted by.  Let's call it `BookSortProperty` and put it in the same file as `BookListParams`. It will 
have values roughly matching those in the application layer, and each value will include a reference to its corresponding
BookSortableColumns layer.

```kotlin
enum class BookSortProperty(val domain: BookSortableColumns) {
    OCLC            (BookSortableColumns.OCLC),
    TITLE           (BookSortableColumns.TITLE),
    PUBLISHER_NAME  (BookSortableColumns.PUBLISHER_NAME),
    ISBN            (BookSortableColumns.ISBN),
    PUBLISHED_YEAR  (BookSortableColumns.PUBLISHED_YEAR),
    AUTHOR_NAME     (BookSortableColumns.FIRST_AUTHOR_NAME)
}
```

> You might be tempted to just use the `BookSortableColumns` enum in the UAPI layer. While we can't stop you from doing
> so, that's generally not a good idea, because you are now tightly coupling the details of your data layer to your
> public API contract. For example, if you rename one of the enum values in your data layer, you'll now have broken
> your API contract, and that's a surefire way to get somebody angry with you. By creating an enum that is specific to
> the UAPI layer of your application, you can prevent issues like this from happening.
> 
> To reinforce this idea, you'll notice that we are creating a sortable property called `AUTHOR_NAME`, but the corresponding
> value in the application layer is `FIRST_AUTHOR_NAME`, because that's what the implementation actually does - use the
> last name of the first author assigned to the book for sorting. This, however, is the kind of implementation detail
> that we don't want to leak up into our clients.
{: .callout-protip }

Now, we need to add this into the `BookListParams` object we already defined, and make `BookListParams` implement
`ListParams.WithSorting`.

```diff
   data class BookListParams(
+    override val sort: SortParams<BookSortProperty>,
     override val subset: SubsetParams
-  ) : ListParams.WithSubset
+  ) : ListParams.WithSort<BookSortProperty>,
+      ListParams.WithSubset
```

> We're adding these new parameters and interfaces in a special order, specially crafted to make the rest of the diffs
> in this document not be as ugly.

`SortParams` is a Runtime-defined class that contains a list of properties and a sort order.

Now, let's add `Listable.WithSorting` to our resource. We'll also need to add some values that we use to generate metadata
and provide default values to you when the user doesn't specify any.:

```diff
  class BooksResource : IdentifiedResource<LibraryUser, Long, Book>,
+                       IdentifiedResource.Listable.WithSort<LibraryUser, Long, Book, BookListParams, BookSortProperty>,
                        IdentifiedResource.Listable.WithSubset<LibraryUser, Long, Book, BookListParams> {
                        
   override fun list(
     userContext: LibraryUser,
     params: BookListParams
   ): ListWithTotal<Book> {
     val result = Library.listBooks(
       includeRestricted = userContext.canViewRestrictedBooks,
+      sortColumns = params.sort.properties.map { it.domain },
+      sortAscending = params.sort.order == SortOrder.ASCENDING,
       subsetSize = params.subset.subsetSize,
       subsetStart = params.subset.subsetStartOffset
     )
     return ListWithTotal(
       totalItems = result.totalItems,
       values = result.list
     )
   }
   
+  override val listDefaultSortProperties: List<BookSortProperty> = listOf(BookSortProperty.TITLE, BookSortProperty.OCLC)
+  override val listDefaultSortOrder: UAPISortOrder = UAPISortOrder.ASCENDING
   override val listDefaultSubsetSize: Int = 50
   override val listMaxSubsetSize: Int = 100
```

Now, if the client doesn't specify sort properties or an order, we'll sort by `title` and `oclc` in ascending order.
The client can specify the sort properties and order via the `sort_properties` and `sort_order` query parameters.

```bash
curl -H "Authorization: Bearer {your OAuth token here}" http://localhost:8080/books?sort_properties=author_name,title&sort_order=descending&subset_size=1
```

> We're just grabbing the first result so that we can fit the response in this document.

```json
{
  "values":[
    {
      "basic":{
        "oclc":{
          "value":26811595,
          "api_type":"read-only",
          "key":true,
          "display_label":"OCLC Control Number"
        },
        "title":{
          "value":"Good Omens",
          "api_type":"modifiable",
          "display_label":"Title"
        },
        "publisher_id":{
          "value":9,
          "description":"Workman",
          "long_description":"Workman Publishing",
          "api_type":"modifiable",
          "display_label":"Publisher"
        },
        "author_ids":{
          "values":[
            {
              "value":8,
              "description":"Terry Pratchett"
            },
            {
              "value":9,
              "description":"Neil Gaiman"
            }
          ],
          "api_type":"modifiable",
          "display_label":"Author(s)"
        },
        
        // ... we've omitted a bunch of properties, because they're REALLY LONG.
        "links":{

        },
        "metadata":{
          "validation_response":{
            "code":200,
            "message":"OK"
          }
        }
      },
      "links":{

      },
      "metadata":{
        "validation_response":{
          "code":200,
          "message":"OK"
        },
        "field_sets_returned":[
          "basic"
        ],
        "field_sets_available":[
          "basic"
        ],
        "field_sets_default":[
          "basic"
        ],
        "contexts_available":{

        }
      }
    }
  ],
  "links":{

  },
  "metadata":{
    "validation_response":{
      "code":200,
      "message":"OK"
    },
    "collection_size":11,
    "sort_properties_available":[
      "oclc",
      "title",
      "publisher_name",
      "isbn",
      "published_year",
      "author_name"
    ],
    "sort_properties_default":[
      "title",
      "oclc"
    ],
    "sort_order_default":"ascending",
    "subset_size":1,
    "subset_start":0,
    "default_subset_size":50,
    "max_subset_size":100
  }
}
```

Now, we get a different result than we would have before - things are in a different order! We're also telling the client
which properties are available for sorting, as well as which ones are used by default.

Sorting - Check!

### Property Naming

By default, the Runtime uses the lower-cased output of your enum's `toString` method. If you don't override
`toString`, this means that the values will be the lower-case versions of your enum values' names. We'll discuss
how to customize this behavior [later](#custom-sort-parsing).

## Filtering

Now that we can get smaller, ordered lists, let's make it so that the client can control which items are included in the
response. One way we can do that is by filtering based the values of specific properties.

The way that we implement this is by allowing you to specify a *filter class*. A filter class is a data class that follows
some specific rules:

* All values must be nullable, collections (i.e `Set<String>`, `List<String>`), or have a default value.
* All values must be either a [Simple Type](../_reference/data-types.md), collection of simple types, or another *filter class* that follows these same rules.

The properties in a filter class should generally match the names of properties in the resource. When using a nested
filter class, it should correspond with the name and properties of a [Subresource](./subresources.md), and allows the
client to filter our base resource by values that are part of a subresource.

When processing filter parameters, you should follow these rules:

* If a parameter receives a collection of values, the returned items should all contain *at least one* of the values, similar to a SQL `IN` clause.
* All specified parameters are joined together by an implicit *AND*. For example, if the client specifies `foo=bar&baz=zop`, each item returned should have 'foo' set to 'bar' AND 'baz' set to 'zop'.

Let's build a filter class for books. For convenience, let's cram it into our existing `BookListParams.kt`.

```kotlin
data class BookFilters(
    val isbns: Set<String>,
    val title: String?,
    val subtitle: String?,
    val publisherIds: Set<Int>,
    val publisherNames: Set<String>,
    val publicationYear: Int?,
    val restricted: Boolean?,
    val authors: AuthorFilters?,
    val genres: GenreFilters?
)

data class AuthorFilters(
    val ids: Set<Int>,
    val names: Set<String>
)

data class GenreFilters(
    val codes: Set<String>,
    val names: Set<String>
)
```

This will translate to the following query parameters:

Name | Type | Example | Description
-----|------|---------|------------
`isbns` | comma-separated strings | `978-0451530653,0-684-83339-5` | Find books that have one of these ISBN numbers
`title` | string | `The+Player+of+Games` | Find titles that exactly match this value
`subtitle` | string | `Book+One+of+the+Stormlight+Archive` | Find books with a subtitle that exactly matches this value
`publisher_ids` | comma-separated integers | `1,4,5` | The book must have one of these publisher IDs
`publisher_names` | comma-separated strings | `Tor,Oxford` | The book must be from one of these publishers
`publication_year` | integer | `1990` | The book must have been published in this year
`authors.ids` | comma-separated integers | `1,3` | The book must have an author with one of these IDs
`authors.names` | comma-separated strings | `Joseph+Heller,Isaac+Asimov` | The book must have an author with one of these names
`genres.codes` | comma-separated strings | `FAN,LOL` | Must have a genre with one of these codes
`genres.names` | comma-separated strings | `Fantasy,Humor` | Must have a genre with one of these names

Field names are mapped to parameter names by turning the camelCase names to snake_case. Where there are nested values,
the name is comprised of the outer field name, a dot, and the nested field name.

When a collection of values is specified, your code should interpret the values as "this property should have one of
these values," much like the `IN` operator in SQL. In addition, the value name should be plural (ending in 's').

> Some things we'd like to add here:
> 
> * A type that says "this string can contain wildcards like '*' and '?'"
> * A type that says "this value can have operators applied, like 'less than' or 'greater than'"
{: .callout-in-progress }

Our domain layer has some corresponding classes. We'll need to translate between them, so let's add some 'toDomain'
methods.

```kotlin
data class BookFilters(
    val isbns: Set<String>,
    val title: String?,
    val subtitle: String?,
    val publisherIds: Set<Int>,
    val publisherNames: Set<String>,
    val publicationYear: Int?,
    val restricted: Boolean?,
    val authors: AuthorFilters?,
    val genres: GenreFilters?
) {
    fun toDomain() = if (hasAnyValues) {
        BookQueryFilters(
            isbn = isbns,
            title = title,
            subtitle = subtitle,
            publisherId = publisherIds,
            publisherNames = publisherNames,
            publicationYear = publicationYear,
            restricted = restricted,
            authors = authors?.toDomain(),
            genres = genres?.toDomain()
        )
    } else {
        null
    }

    val hasAnyValues =
        isbns.isNotEmpty()
            || title != null
            || subtitle != null
            || publisherIds.isNotEmpty()
            || publisherNames.isNotEmpty()
            || publicationYear != null
            || restricted != null
            || (authors == null || authors.hasAnyValues)
            || (genres == null || genres.hasAnyValues)
}

data class AuthorFilters(
    val ids: Set<Int>,
    val names: Set<String>
) {
    fun toDomain() = if (hasAnyValues) {
        AuthorQueryFilters(
            id = ids,
            name = names
        )
    } else {
        null
    }

    val hasAnyValues = ids.isNotEmpty() || names.isNotEmpty()
}

data class GenreFilters(
    val codes: Set<String>,
    val names: Set<String>
) {
    fun toDomain() = if (hasAnyValues) {
        GenreQueryFilters(
            code = codes,
            name = names
        )
    } else {
        null
    }

    val hasAnyValues = codes.isNotEmpty() || names.isNotEmpty()
}
```

> It might feel wasteful to do these conversions, but, just like with `BookSortProperties`, this separation of concerns
> (api queries vs. database queries) makes it harder to accidentally break our API's consumers by isolating changes
> in the higher and lower layers of the application from each other.
{: .callout-protip }

Next, we need to add our filters to our parameter class:

```diff
  data class BookListParams(
    override val sort: SortParams<BookSortProperty>,
+   override val filters: BookFilters?,
    override val subset: SubsetParams
  ) : ListParams.WithSort<BookSortProperty>,
+     ListParams.WithFilters<BookFilters>,
      ListParams.WithSubset
```

Finally, let's add support to our resource class.

```diff
  class BooksResource : IdentifiedResource<LibraryUser, Long, Book>,
                        IdentifiedResource.Listable.WithSort<LibraryUser, Long, Book, BookListParams, BookSortProperty>,
+                       IdentifiedResource.Listable.WithFilters<LibraryUser, Long, Book, BookListParams, BookFilters>,
                        IdentifiedResource.Listable.WithSubset<LibraryUser, Long, Book, BookListParams>
  {

    override fun list(
      userContext: LibraryUser,
      params: BookListParams
    ): ListWithTotal<Book> {
      val result = Library.listBooks(
        includeRestricted = userContext.canViewRestrictedBooks,
        sortColumns = params.sort.properties.map { it.domain },
        sortAscending = params.sort.order == UAPISortOrder.ASCENDING,
+       filters = params.filters?.toDomain(),
        subsetSize = params.subset.subsetSize,
        subsetStart = params.subset.subsetStartOffset
      )
      return ListWithTotal(
        totalItems = result.totalItems,
        values = result.list
      )
    }

    override val listDefaultSortProperties: List<BookSortProperty> = listOf(BookSortProperty.TITLE, BookSortProperty.OCLC)
    override val listDefaultSortOrder: UAPISortOrder = UAPISortOrder.ASCENDING
    override val listDefaultSubsetSize: Int = 50
    override val listMaxSubsetSize: Int = 100
```

Finally, we can filter our API requests!  Let's get every book in the 'Humor' genre:

```bash
curl -H "Authorization: Bearer {your OAuth token here}" http://localhost:8080/books?genres.names=Humor&subset_size=1
```

```json
{
  "values":[
    {
      "basic":{
        "oclc":{
          "value":35231812,
          "api_type":"read-only",
          "key":true,
          "display_label":"OCLC Control Number"
        },
        "title":{
          "value":"Catch-22",
          "api_type":"modifiable",
          "display_label":"Title"
        },
        "genres":{
          "values":[
            {
              "value":"FI",
              "description":"Fiction"
            },
            {
              "value":"HFI",
              "description":"Historical Fiction"
            },
            {
              "value":"LOL",
              "description":"Humor"
            }
          ],
          "api_type":"modifiable",
          "display_label":"Genre(s)"
        }
        // ... we've omitted a bunch of properties, because they're REALLY LONG.
      }
    }
  ],
  "links":{

  },
  "metadata":{
    "validation_response":{
      "code":200,
      "message":"OK"
    },
    "collection_size":2,
    "sort_properties_available":[
      "oclc",
      "title",
      "publisher_name",
      "isbn",
      "published_year",
      "author_name"
    ],
    "sort_properties_default":[
      "title",
      "oclc"
    ],
    "sort_order_default":"ascending",
    "subset_size":1,
    "subset_start":0,
    "default_subset_size":50,
    "max_subset_size":100
  }
}
```

## Searching

Filters are great, but they can't solve everything. Users are increasingly used to being able to do a full-text, fuzzy-match
search, like they can with a search engine. But, because all filters are implicitly joined with *AND*, you can't
build a query like "give me every book with the word 'Foundation' in either its title or subtitle."

That's where searching comes in. Searching is treated separately from filtering because it has very different semantics.

Search queries in the UAPI must specify both the text to search for and a *search context*. This search context is a name
for a collection of related fields, all of which will be searched for the search text. For example, a 'persons' API
might have a search context of 'names', which would search 'surnames', 'given_names', and 'preferred_name'.

Searching can be used many different ways. We'll show two different ways of using them - full-text search and filtering
across multiple fields.

> If you implement full-text search with a fancy search backend, like ElasticSearch, you can often have the search
> backend tell you the 'relevance' of a result. In such cases, it can be very helpful to clients to provide a
> `SEARCH_RELEVANCE` sort 'property.' Just make sure you send back an [error](./errors.md) if the client doesn't ask
> for a search and it doesn't make sense to sort by relevance without it!
{: .callout-protip }

> Searching is something that should be carefully planned before implementing. Naive search implementations (like the
> one in our example application) can give end users a less-than-desirable experience and can have major performance
> implications on the API. It's best to move slowly and carefully when choosing to allow search or adding new search
> contexts.
{: .callout-warning }


Search support adds the following query parameters and metadata values:

Query Parameter | Type | Description
----------------|------|------------
search_text	| string | text to search for
search_context | enum value | The context in which to perform the search

Metadata Value | Description
---------------|------------
search_contexts_available | A map of valid `search_context` values to a list of the properties they each search.

Like with sorting, in order to specify our search contexts, we use an enum class.  This enum class will be serialized
using the same rules as a sort property enum (by turning it into lowercase snake_case).

Once again, we have a separate search construct in the domain layer. Our domain layer accepts search queries via a
'search' property in `BookQueryFilters`.

> Yeah, we're hitting you hard with the "separate your domain and API layer concerns" stick. Deal with it.

Let's start off by defining our search contexts. Once again, we'll shove it in `BookListParams.kt`:

```kotlin
enum class BookSearchContext {
    TITLES,
    AUTHORS,
    GENRES,
    CONTROL_NUMBERS;
}
```

We'll also add a function to take a search context and search text and turn it into one of our domain's search representations:

```kotlin
fun BookSearchContext.toDomain(searchText: String) = when(this) {
    BookSearchContext.TITLES -> BookTitleSearch(searchText)
    BookSearchContext.AUTHORS -> BookAuthorSearch(searchText)
    BookSearchContext.GENRES -> BookGenreSearch(searchText)
    BookSearchContext.CONTROL_NUMBERS -> BookControlNumbersSearch(searchText)
}
```

> This uses a Kotlin 'when' expression. They're like `switch`, but better! For example, if you add a new value to
> `BookSearchContext` but forget to add it here, the code will not compile. Neat!
> 
> The domain layer here is using 'sealed classes', which are like super-powered enums. You should definitely check them
> out. They're very powerful, especially when coupled with `when`
{: .callout-kotlin }

This will allow full-text search on titles and subtitles, author names, and genre codes and names. In addition, given
an unknown type of control number (ISBN or OCLC), we can get back any matching books, no matter which type of control number
it is.

Now, we need to update `BookListParams`.

```diff
  data class BookListParams(
    override val sort: SortParams<BookSortProperty>,
    override val filters: BookFilters?,
+   override val search: SearchParams<BookSearchContext>?,
    override val subset: SubsetParams
  ) : ListParams.WithSort<BookSortProperty>,
      ListParams.WithFilters<BookFilters>,
+     ListParams.WithSearch<BookSearchContext>,
      ListParams.WithSubset
```

To finish it off, let's add our implementation to our resource.

```diff

  class BooksResource : IdentifiedResource<LibraryUser, Long, Book>,
                        IdentifiedResource.Listable.WithSort<LibraryUser, Long, Book, BookListParams, BookSortProperty>,
                        IdentifiedResource.Listable.WithFilters<LibraryUser, Long, Book, BookListParams, BookFilters>,
+                       IdentifiedResource.Listable.WithSearch<LibraryUser, Long, Book, BookListParams, BookSearchContext>,
                        IdentifiedResource.Listable.WithSubset<LibraryUser, Long, Book, BookListParams>
  {
    override fun list(
        userContext: LibraryUser,
        params: BookListParams
        ): ListWithTotal<Book> {
+       val search = params.search?.run { context.toDomain(text) }
        val result = Library.listBooks(
            includeRestricted = userContext.canViewRestrictedBooks,
            sortColumns = params.sort.properties.map { it.domain },
            sortAscending = params.sort.order == UAPISortOrder.ASCENDING,
            filters = params.filters?.toDomain(),
+           search = search,
            subsetSize = params.subset.subsetSize,
            subsetStart = params.subset.subsetStartOffset
        )
        return ListWithTotal(
            totalItems = result.totalItems,
            values = result.list
        )
    }

    override val listDefaultSortProperties: List<BookSortProperty> = listOf(BookSortProperty.TITLE, BookSortProperty.OCLC)
    override val listDefaultSortOrder: UAPISortOrder = UAPISortOrder.ASCENDING
    override val listDefaultSubsetSize: Int = 50
    override val listMaxSubsetSize: Int = 100
+   override fun listSearchContexts(value: BookSearchContext) = when(value) {
+       BookSearchContext.TITLES -> listOf("title", "subtitles")
+       BookSearchContext.AUTHORS -> listOf("authors.name")
+       BookSearchContext.GENRES -> listOf("genres.codes", "genres.name")
+       BookSearchContext.CONTROL_NUMBERS -> listOf("oclc", "isbn")
+   }
```

`listSearchContexts` is a method we must implement in order to give the runtime the knowledge it needs to construct our
metadata for us. It maps search context names to the list of properties that they search. This can include properties 
on sub-resources, as you can see in the example (authors.name, genres.name, etc.).
It is purely informational, and doesn't affect the actual implementation of your searching. That means
that the UAPI runtime can't enforce that this information is valid and up-to-date, but please, do your clients a favor
and maintain this list.

> `listSearchContexts` will only be called once, and could normally be a `val`. However, in order to let you rely on
> the compiler to make sure that you don't forget a value, we made it a function so you can use `when` and it's superpowers.
{: .callout-kotlin }

To make sure we've got everything right, let's search for books which have an author with 'isaac' in their name.

```bash
curl -H "Authorization: Bearer {your OAuth token here}" http://localhost:8080/books?search_text=isaac&search_context=authors
```

```json
{
  "values":[
    {
      "basic":{
        "oclc":{
          "value":53896777,
          "api_type":"read-only",
          "key":true,
          "display_label":"OCLC Control Number"
        },
        "title":{
          "value":"Foundation",
          "api_type":"modifiable",
          "display_label":"Title"
        },
        "author_ids":{
          "values":[
            {
              "value":4,
              "description":"Isaac Asimov"
            }
          ],
          "api_type":"modifiable",
          "display_label":"Author(s)"
        }
        // ... we've omitted a bunch of properties, because they're REALLY LONG.
        
      }
    }
  ],
  "links":{

  },
  "metadata":{
    "validation_response":{
      "code":200,
      "message":"OK"
    },
    "collection_size":1,
    "sort_properties_available":[
      "oclc",
      "title",
      "publisher_name",
      "isbn",
      "published_year",
      "author_name"
    ],
    "sort_properties_default":[
      "title",
      "oclc"
    ],
    "sort_order_default":"ascending",
    "search_contexts_available":{
      "titles":[
        "title",
        "subtitles"
      ],
      "authors":[
        "authors.name"
      ],
      "genres":[
        "genres.codes",
        "genres.name"
      ],
      "control_numbers":[
        "oclc",
        "isbn"
      ]
    },
    "subset_size":1,
    "subset_start":0,
    "default_subset_size":50,
    "max_subset_size":100
  }
}
```

Look! We got back our one and only book by the great Isaac Asimov!

# Customizing List Features

> To be written
{: .callout-in-progress }

## Custom Sort Parsing

> To be written
{: .callout-in-progress }

## Custom Filter Parsing

> To be written
{: .callout-in-progress }

## Custom Search Parsing

> To be written
{: .callout-in-progress }

# Best Practices

> To be written
{: .callout-in-progress }

# Summary

Well, we've learned how to list resources. Things got a little weird there, but you made it through! Here's a picture
of a dancing Ron Swanson as a reward:

![Dancing Ron Swanson](https://media.giphy.com/media/zyin7TYoGmLAs/giphy.gif)

Next up, we'll learn about how to mutate your resources by exposing them to radioactive spider bites! I can't wait to see what happens!

![Spider-man makes his entrance](https://media.giphy.com/media/3o7abooVPgeGpknXpu/giphy.gif)

Oh, we're not talking about that kind of mutation? Oh well. Onward ho!
