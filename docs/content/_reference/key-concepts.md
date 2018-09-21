---
title: Key Concepts
order: 1
---

# Key Concepts and Terms

There are several concepts and terms that are used in this documentation that you should be sure to understand.

# Key Terms

## Runtime

The core interface you will interact with is `UAPIRuntime`.  This class contains all of the logic needed to handle requests
and route them to your code. It also knows how to describe your API so that external tools and clients can consume it.

## Resource

A **Resource** is the top-level concept in a UAPI implementation. An API can have one or more resources.  Each resource
has a name and encapsulates all operations that can be performed on a type of data.

See [The UAPI Specification](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#30-resources)
for a detailed explanation of resources.

There are two types of resources: *Identified* and *Singleton*.  Most resources are *Identified*, meaning that there are
multiple records of that resource type, each of which has a unique identifier.  *Singleton* resources are very rare. If 
you don't know what kind of resource you're working with, you're almost certainly dealing with an *Identified* resource.

A **Resource** always has a name.  For an *Identified* resource, this is always a plural name: 'students' or 'employees'
or 'persons'. This name appears in the path to a resource.

*Identified* resources are represented as instances of the `IdentifiedResource` interface. *Singleton* resources implement
the `SingletonResource` interface.

```
|    resource    |
/persons/{byu_id}/jobs/{job_id}/
```

## Subresource

A **Subresource** is an entity that is nested within a **Resource**. A resource can have any number of subresources,
but only one level of of subresources is allowed (i.e, you cannot nest a subresource inside another subresource).

See [The UAPI Specification](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#324-representing-sub-resources)
for a detailed explanation of subresources.

```
|    resource    | subresource |
/persons/{byu_id}/jobs/{job_id}/
```

**TODO:** When to put things in a subresource (especially a singleton subresource)

## Fieldset

For all intents and purposes, this is another name for a **Subresource**. You don't need to worry about the differences;
the Runtime handles translating between the two concepts.

## Data Domains

> Resources often have associated sets of terms, like accepted state and country names and their abbreviations, that 
> define possible values for properties. These terms, or controlled vocabularies, are necessary for a client to properly 
> use the API. 
>
> -- [University API Specification](https://github.com/byu-oit/UAPI-Specification/blob/master/University%20API%20Specification.md#80-meta-data-sets)

A 'Data Domain' is a controlled set of values that are acceptable values for a given field. Some examples of data domains
include a list of U.S. States, the months in a year, or campus buildings.

Data domains can have a value (such as a state code or building code), an optional description (commonly-used state name), 
and an optional long description (such as the full name of a state).

In Kotlin, data domains are often represented as Enums.  They may also be a small set of database-driven values which 
are not modifiable through the API.

### Example Data Domains

Domain | Value | Description | Long Description
-------|-------|-------------|------------------
States | `MA`  | Massachusetts | Commonwealth of Massachusetts
Months | `01`   | January       |
Buildings | `ITB` | Information Technology Building |
Time Units | `MS` | Milliseconds |
Genres     | `SF` | Science Fiction |
Time Zones | `MDT` | Mountain Daylight Time |
Grades     | `B+` | |
Employee Type | `FT` | Full-Time Employee |


### What is a domain and what is a resource?

The line between a domain and a resource is often blurry. In some cases, a certain value may be a domain in one system,
but a resource in another. For example, if your API is for classroom scheduling, it may make sense to have campus buildings
be a domain. If, however, your API represents physical campus resources, it would make sense for buildings to be a top-level
resource.  It is even possible that the domain values in the scheduling API may be driven by calls to the campus resource
API.

Data Domain | Resource
------------|---------
Changes very infrequently | Changes more frequently
Effectively Static |  Can be created via the API
Value with optional descriptions | Arbitrary data
No relationships | Can have relationships with other resources
Public Data | May require authorization


