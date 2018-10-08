---
title: Generating Response Bodies
short_title: Responses
order: 5
tutorial_source: https://github.com/byu-oit/kotlin-uapi/tree/master/examples/library/tutorial-steps/5-response-body
---

In our last section, we stubbed out a response body containing two basic fields: `oclc` and `title`.
In this section, we're going to expand the list of fields, as well as adding a lot of useful metadata
to them, in accordance with the University API Specification.

# Review of the Specification

If you've read the UAPI specification, you know that there is no such thing as a simple key-value field. If you haven't,
well, then, *surprise!*

This means that fields in a response don't just look like this:

```json
{
  "title": "A Player of Games"
}
```

Instead, we wrap the value with a bunch of metadata:

```json
{
  "title": {
    "value": "A Player of Games",
    "api_type": "read-only",
    "display_label": "Title"
  }
}
```

Like we said, *surprise!* Go on, get it out of your system, and then come back when you want to hear the reasons *why* we
would do this.

We do this so as to provide information to the client about both the field and the value of the field. 
The intention is to simplify the development of client applications, especially Web UIs, by providing them with the information
they need to be able to drive decisions that would otherwise require the addition of business logic to the client.

There are three groupings of these metadata values: Field-Related, Value-Derived, and Contextual.

## Field-Related Metadata

Field-related metadata describes the *field* they are a part of, and do not change based on the value of the field. In
general, every response from the API will return the same values for these metadata (in fact, this runtime enforces that rule).

Name | Type | Description
-----|------|------------
`key`  | boolean | `true` if the property is part of the identifier (the one contained in the URL) of the resource. If not `true`, is generally not set at all.
`display_label` | string | A suggested label for a UI to use for this property. Shouldn't be longer than 30 characters.
`domain` | URI | Link describing the allowed values. See the [chapter on Domains](./domains.md).

## Value-Derived Metadata

Value-Derived metadata is derived from the actual value of a field. These metadata do not describe the field itself,
but rather the specific value in it. If they are present for one value of a field, they should be present for all values
(we enforce this rule too!).

Name | Type | Description | Example
-----|------|-------------|---------
`description` | string | A short, human-friendly description of the value. Generally limited to 30 characters. | For a state, this might be the common name of the state, like 'Virginia'.
`long_description` | string | A slightly longer description of the value. Generally limited to 256 characters. | For a state, this might be the full name of the state, such as 'Commonwealth of Virgina'.
`related_resource` | URI | A link to a related URL, generally the 'canonical' representation of this value. If this is set, the `api_type` should be `'related'`. | For a user ID, this would point to their record in the central Identity system.

## Contextual Metadata

Contextual metadata are driven by the larger context in which the field and value exist. They are driven by a combination
of the field, the value of the field, and some other context, such as the state of the Resource and the client's authorizations.

Name | Type | Description
-----|------|------------
`api_type` | String Enum | Describes how the value is to be used within the API.

You won't have to worry about API Type directly, as the Runtime computes it based on various factors.  However, it is
useful to know what the allowed values are and what they mean.

`api_type` | Description | Examples
-----------|-------------|----------
`"system"` | Denotes that this value was assigned by the system, and is not ever modifiable through the API. | Last-updated time fields, generated identifiers
`"modifiable"` | The value may be modified through the API, and the current user is allowed to do so. | 
`"read-only"` | The value is not modifiable. This may be always true for the field, or it may reflect the current user's permissions. | 
`"derived"` | The value is derived from some other fields. | Student GPA, class standing, etc.
`"related"` | The responsibility for manipulating this property doesn't belong to this API. The business logic to modify the property exists in the related_resource. | The ID of a user from a central Identity system

## Value Fields

The value of the field is mapped to a different key, depending on what type of data it contains.

Key | Data Type
-----------|-----------
`"value"` | Simple values (strings, numbers, booleans)
`"values"` | Arrays of simple values + metadata
`"object"` | Complex objects + metadata
`"objects"` | Arrays of complex objects + metadata

# Defining Properties

Now that we've reviewed what the Specification allows, let's talk about how to actually add all of this metadata.

