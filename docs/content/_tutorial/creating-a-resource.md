---
title: Creating a Resource
order: 4
tutorial_source: https://github.com/byu-oit/kotlin-uapi/tree/master/examples/library/tutorial-steps/4-creating-a-resource
---

# Contents
{:.no_toc}

* This will become the Table of Contents
{:toc}

It's finally time to create an actual API endpoint!

A Resource is created by implementing one of two interfaces: `IdentifiedResource` or `SingletonResource`. Singleton
resources are rarely used, so we'll just implement an `IdentifiedResource`.

# Stubbing out a Resource

Create a file named `BooksResource.kt`. You will probably want to put it in the same directory as your `User` classes,
`src/main/kotlin/edu/byu/uapi/library/`.

```kotlin
package edu.byu.uapi.library

import edu.byu.uapi.kotlin.examples.library.Book
import edu.byu.uapi.kotlin.examples.library.Library
import edu.byu.uapi.server.resources.identified.IdentifiedResource
import edu.byu.uapi.server.resources.identified.fields
import kotlin.reflect.KClass

class BooksResource : IdentifiedResource<LibraryUser, Long, Book> {

}
```

There are three generic parameters that every Identified Resource needs.

The first type parameter (`LibraryUser`) is the User Context type. This must match the type you used in your main class.

The second type parameter (`Long`) is the type of the identifier used to get instances of this resource. This is the type
of the value we'll extract from the URL path variables. Most of the time, this will be a simple type, like a String
or a Number. We'll cover more complex types later.

The third type is your resource's 'Model'.  This is the class that we will pass to all of the methods you define
to represent the actual instance of the resource that is being manipulated. This class should contain most, if not all,
of the information needed to render a response to the client. The details of that rendering will be covered later.

Next, we need to register your resource with the UAPI Runtime. You can do that in your main class (`LibraryApp.kt`):

```kotlin
  val runtime = UAPIRuntime(actualUserFactory)

  // All of our configuration is going to go here
  runtime.register("books", BooksResource())
  
```

# Making it compile

You may have noticed that your `BooksResource` doesn't compile. That's because we haven't implemented the vals and methods
defined in the interface yet! So, let's go back to that file and implement these methods.

First, we need to have a way to load a Model instance from an ID. We'll call the static `Library` object, which will load
a `Book` from the database:

```kotlin
    override fun loadModel(
        userContext: LibraryUser,
        id: Long
    ): Book? {
        return Library.getBook(id)
    }
```

`loadModel` accepts an instance of your userContext and your ID type, and returns an instance of your model or `null`.
If there is no model with the given ID, you should return `null`, not raise an error. Don't worry about enforcing
user authorization rules here; we'll enforce that in another method.

Next, we need to have a way to extract a resource's identifier from its Model. A `Book` stores its ID in the `oclc` field:

```kotlin
    override fun idFromModel(model: Book): Long {
        return model.oclc
    }
```

Now, we need to check to see if the user can view a resource. At our library, we allow anyone to see the list of books
in our catalog. We'll cover authorizations for modifying a `Book` when we add modification methods.

```kotlin
    override fun canUserViewModel(
        userContext: LibraryUser,
        id: Long,
        model: Book
    ): Boolean {
        return true // Anybody can view our basic catalog information
    }
```

We also need to describe the responses we send when someone loads our resource. We'll cover the details in the 
[next chapter](./response-bodies.md); for now, just copy the following stub:

```kotlin
    override val responseFields = fields {
        value<Long>("oclc") {
            getValue { book -> book.oclc }
        }
        value<String>("title") {
            getValue { book -> book.title }
        }
    }
```

This defines two fields for our response, "oclc" and "title". It also specifies (in `getValue`) how to get the value of each
field from an instance of `Book`.

{% include callouts/code.html content="Your completed resource should look like [this](https://github.com/byu-oit/kotlin-uapi/blob/master/examples/library/tutorial-steps/4-creating-a-resource/src/main/kotlin/edu/byu/uapi/library/BooksResource.kt)." %}

# Let's make an API call!

Now, if you run your API, it should start! You should be able to use WSO2 OAuth credentials to to call your API.
Our library database comes with a pre-loaded list of Books; try loading these and see what they are!

* 26811595
* 890303755
* 733291011
* 35231812
* 799352269
* 889161015
* 969863614
* 53896777
* 23033258
* 71126670

Let's load the first one, *Good Omens*, OCLC #26811595. Using the REST client of your choice, make a call to `http://localhost:8080/books/26811595`.
Don't forget to include an OAuth Bearer token!

Here's what a request using `cURL` might look like:

```bash
curl -H "Authorization: Bearer {your OAuth token here}" http://localhost:8080/books/26811595
```

This should return a JSON response like:

```json
{
  "basic": {
    "oclc": {
      "value": 26811595,
      "api_type": "read-only"
    },
    "title": {
      "value": "Good Omens",
      "api_type": "read-only"
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
}
```

# Resource Identifiers

There are two ways to identify a resource: a simple, one-value ID or a multi-value compound ID.

## Simple Identifiers

Most resources are identified by a simple identifier, like a single number or string. In our Books example, books
are identified by one value - their OCLC number.

Examples of simple identifiers include:

Pattern | Example
--------|---------
`/books/{oclc}` | `/books/26811595`
`/persons/{byu_id}` | `/persons/123456889`
`/states/{state_code}` | `/states/UT`
`/grains-of-sand/{uuid}` | `/grains-of-sand/c71bd1dc-adee-406c-bc3b-17118145ec0e`

The UAPI runtime knows how to handle a [number of simple types](../_reference/data-types.md). These types include most
simple data types that are built-in to the JVM: strings, numbers,  UUIDs, etc. Also, by default, any enum class is
acceptable, with the output of the `toString` method governing how the value is serialized and deserialized.

We'll talk about how to add more classes to the list in the chapter "[Advanced Options](./advanced-options.md)."

## Compound Identifiers

The [UAPI Spec](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#214-composite-resource-identifier)
allows for 'Compound Identifiers' - Resource IDs that are made up of more than one value. For example, a class section
might be represented by its course identifier, the term in which it is taught, and the section number: `/classes/{course_id},{term},{section}`.

This could be represented by creating a data class called `SectionId`:

```kotlin
data class SectionId(
  val courseId: String,
  val term: String,
  val section: Int
)
```

You can then use this ID as the id type in a resource:

```kotlin
class ClassSectionResource : IdentifiedResource<MyUser, SectionId, ClassSection> {
  // Minor details, like the actual code, go here
}
```

By default, the Runtime knows how to create composite identifiers from [data classes](https://kotlinlang.org/docs/reference/data-classes.html),
with the following conditions:

* The class' properties must all be known data types, as explained in [Simple Identifiers](#simple-identifiers)
* The class' properties should be non-nullable. Technically, they can be nullable, but they will always be set with a non-null value.

The URL path parameters will be in the same order as the properties in the data class' constructor. The path parameters
will be constructed from the names of the properties, separated by commas (`,`). 

## Customizing ID Parsing

If you need to use ID types that aren't supported by default, or if you want to control how they are parsed, you can
implement a custom `IdParamReader`. By default, the Runtime uses an IdParamReader that uses reflection to analyze
your ID type.

The implementation of a custom ID param reader is described in the [reference section](../_reference/customizing-input-parsing.md).

Once you have implemented a custom reader, you can tell the runtime about it by overriding the `getIdReader` function:

```kotlin
class MyResource : IdentifiedResource<MyUser, MyCustomId, MyDomain> {
  
  override fun getIdReader(
    typeDictionary: TypeDictionary,
    paramPrefix: String
  ): IdParamReader<MyCustomId> {
    // Create your custom ID reader here
  }
    
  // Minor details, like the actual code, go here
}
```

This method takes in two parameters: a `TypeDictionary`, which gives you access to the data types that the Runtime knows
about, and a `paramPrefix`, which should be prepended to the names of the path parameters. 

# Moving On...

Next, we'll learn about construction of our response body.
