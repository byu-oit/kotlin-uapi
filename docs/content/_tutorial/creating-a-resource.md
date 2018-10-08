---
title: Creating a Resource
order: 4
tutorial_source: https://github.com/byu-oit/kotlin-uapi/tree/master/examples/library/tutorial-steps/4-creating-a-resource
---

It's finally time to create an actual API endpoint!

A Resource is created by implementing one of two interfaces: `IdentifiedResource` or `SingletonResource`. Singleton
resources are rarely used, so we'll just implement an `IdentifiedResource`.

## Stubbing out a Resource

Create a file named `BooksResource.kt`. You will probably want to put it in the same directory as your `User` classes,
`src/main/kotlin/edu/byu/uapi/library/`.

```kotlin

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

## Making it compile

You may have noticed that your `BooksResource` doesn't compile. That's because we haven't implemented the vals and methods
defined in the interface yet! So, let's go back to that file and implement these methods.

First, thanks to the erased nature of generics on the JVM, we have to say again what our ID type is:

```kotlin
    override val idType = Long::class
```

Now, we need to have a way to load a Model instance from an ID. We'll call the static `Library` object, which will load
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

We also need to describe the responses we send when someone loads our resource. We'll cover the details in the next
chapter; for now, just copy the following stub:

```kotlin
    override val responseFields: List<ResponseField<LibraryUser, Book, *>> = uapiResponse {
        value(Book::oclc) {
            
        }
        value(Book::title) {
        
        }
    }
```

{% include callouts/code.html content="Your completed resource should look like [this](https://github.com/byu-oit/kotlin-uapi/blob/master/examples/library/tutorial-steps/4-creating-a-resource/src/main/kotlin/edu/byu/uapi/library/BookResource.kt)." %}

Now, if you run your API, it should start! You should be able to use WSO2 OAuth credentials to to call your API.
Our library database comes with a pre-loaded list of Books; try loading these and see what they are!

* 890303755
* 733291011
* 35231812
* 799352269
* 889161015
* 969863614
* 53896777
* 23033258

Let's load the first one. Using the REST client of your choice, make a call to `http://localhost:8080/books/890303755`.
Don't forget to include an OAuth Bearer token!

Here's what a request using `cURL` might look like:

```bash
curl -H "Authorization: Bearer {your OAuth token here}" http://localhost:8080/books/890303755
```

This should return a JSON response like:

```json
{
  "basic": {
    "oclc": {
      "value": 890303755,
      "api_type": "read-only"
    },
    "title": {
      "value": "The Player of Games",
      "api_type": "read-only"
    },
    "links": {},
    "metadata": {
      "validation_response": {
        "code": 200,
        "message": "OK"
      },
      "validation_information": []
    }
  },
  "links": {},
  "metadata": {
    "validation_response": {
      "code": 200,
      "message": "OK"
    },
    "validation_information": [],
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

Next, we'll learn about construction of our response body.
