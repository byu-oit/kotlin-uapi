# Key Concepts and Terms

There are several concepts and terms that are used in this documentation that you should be sure to understand.

# Key Terms

## Runtime

The core interface you will interact with is `UAPIRuntime`.  This class contains all of the logic needed to handle requests
and route them to your code. It also knows how to describe your API so that external tools and clients can consume it.

TODO: Examples on how to create a runtime.

## Resource

A **Resource** is the top-level concept in a UAPI implementation. An API can have one or more resources.  Each resource
has a name and encapsulates all operations that can be performed on a type of data.

See [The UAPI Specification](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#30-resources)
for a detailed explanation of resources.

**Resources** are registered with a **Runtime**, which examines and validates the resource, then sets up all of the handlers
needed to serve requests to that resource.

There are two types of resources: *Identified* and *Singleton*.  Most resources are *Identified*, meaning that there are
multiple records of that resource type, each of which has a unique identifier.  *Singleton* resources are very rare. If 
you don't know what kind of resource you're working with, you're almost certainly dealing with an *Identified* resource.

A **Resource** always has a name.  For an *Identified* resource, this is always a plural name: 'students' or 'employees'
or 'persons'. This name appears in the path to a resource.

*Identified* resources are represented as instances of the `IdentifiedResource` interface. *Singleton* resources implement
the `SingletonResource` interface.

## Subresource

A **Subresource** is an entity that is nested within a **Resource**. A resource can have any number of subresources,
but only one level of of subresources is allowed (i.e, you cannot nest a subresource inside another subresource).

See [The UAPI Specification](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#324-representing-sub-resources)
for a detailed explanation of subresources.

## Fieldset

For all intents and purposes, this is another name for a **Subresource**. You don't need to worry about the differences;
the Runtime handles translating between the two concepts.

# Common Function Parameters

## User Context (`userContext`)

The Runtime will take care of authorizing incoming requests, relying on BYU's OAuth infrastructure. In order to expose
that information to your code, your implementation will be passed a **User Context** whenever necessary.

This **User Context** is a class which is specific to your application. All **Resources** in the same **Runtime** must
use the same type for their User context.

When you construct a Runtime instance, you must provide a `UserContextCreator`.  This is a function which, when provided
with Authentication information (such as the provided, validated JWT) will return an instance of the User Context type.

TODO: This could probably use some concrete examples and maybe some default implementations.

## Validation

Whenever an input is passed as part of an operation, such as in a Create, you will need to provide a function which
validates that input.  This function will be passed an instance of the `Validating` class.  This class can be used
to collect a series of assertions about the input, and will then return to the caller a list of all of the assertions which failed.

See 
