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
        "publisher_id":{
          "value":1,
          "description":"Simon & Schuster",
          "long_description":null,
          "api_type":"modifiable",
          "display_label":"Publisher"
        },
        "available_copies":{
          "value":3,
          "api_type":"derived",
          "display_label":"Available Copies"
        },
        "isbn":{
          "value":"0-684-83339-5",
          "api_type":"system",
          "display_label":"ISBN"
        },
        "subtitles":{
          "values":[

          ],
          "api_type":"modifiable",
          "display_label":"Subtitles"
        },
        "author_ids":{
          "values":[
            {
              "value":2,
              "description":"Joseph Heller"
            }
          ],
          "api_type":"modifiable",
          "display_label":"Author(s)"
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
        },
        "published_year":{
          "value":1961,
          "api_type":"modifiable",
          "display_label":"Publication Year"
        },
        "restricted":{
          "value":false,
          "api_type":"modifiable",
          "display_label":"Is Restricted"
        },
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
        "available_copies":{
          "value":1,
          "api_type":"derived",
          "display_label":"Available Copies"
        },
        "isbn":{
          "value":"0-575-04800-X",
          "api_type":"system",
          "display_label":"ISBN"
        },
        "subtitles":{
          "values":[
            {
              "value":"The Nice and Accurate Prophecies of Agnes Nutter, Witch"
            }
          ],
          "api_type":"modifiable",
          "display_label":"Subtitles"
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
        "genres":{
          "values":[
            {
              "value":"FAN",
              "description":"Fantasy"
            },
            {
              "value":"FI",
              "description":"Fiction"
            },
            {
              "value":"LOL",
              "description":"Humor"
            }
          ],
          "api_type":"modifiable",
          "display_label":"Genre(s)"
        },
        "published_year":{
          "value":1990,
          "api_type":"modifiable",
          "display_label":"Publication Year"
        },
        "restricted":{
          "value":false,
          "api_type":"modifiable",
          "display_label":"Is Restricted"
        },
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



## Searching

# Customizing List Features

## Custom Sort Parsing

## Custom Filter Parsing

## Custom Search Parsing

