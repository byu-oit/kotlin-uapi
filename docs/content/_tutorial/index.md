---
title: Tutorial
---

This tutorial will walk you through creating an API that is compatible with the University API Standard, using the
BYU UAPI Kotlin Runtime.

Before working through this tutorial, you should familiarize yourself with the [UAPI Key Concepts](../_reference/key-concepts.md).

{% capture code_on_github %}
The code for each step of the tutorial can be viewed 
[on GitHub](https://github.com/byu-oit/kotlin-uapi/tree/master/examples/library/tutorial-steps).
{% endcapture %}
{% include callouts/code.html content=code_on_github %}

## Library API

The API we will be creating represents the Terminus Public Library & Encyclopedia Repository. 
The library only has books, as it is run by members of the Most Pure and Undefiled Order of Librarians.

In this library's API, there are three levels of access: Public, Cardholder, and Librarian.

The Library API has three root [resources](../_reference/key-concepts.md#resource): `books`, `authors`, `cardholders`, and `loans`. 
Some resources have one or more [subresources](../_reference/key-concepts.md#subresource).

Resource | Identifier | Subresources | Description
---------|------------|--------------|-------------
`books`  | [OCLC number](https://en.wikipedia.org/wiki/OCLC#Identifiers_and_linked_data) | `authors`, `genres`, `copies` | Represents all book titles contained in the library.
`authors` | Generated author ID | `books` | List of all known authors
`cardholders` | Generated cardholder ID | `active-loans`, `loan-history`, `ban` | Represents everyone who has a library card.
`loans` | Composite of book OCLC, Copy ID, cardholder ID, and date | | When a cardholder checks out a book, a `loan` is created.

In addition, we will define two [Data Domains](../_reference/key-concepts.md#data-domains): `publishers` and `genres`.

Our final path structure will look like this:

* `/books/{oclc_num}/`
    * `/authors/{id}`
    * `/genres/{id}`
    * `/copies/{id}`
* `/authors/{id}`
    * `/books/{id}`
* `/cardholders/{id}`
    * `/active-loans/{id}`
    * `/loan-history/{id}`
    * `/ban`
* `/loans/{oclc_num},{copy_id},{cardholder id},{date}`
* `/meta`
    * `/publishers`
    * `/genres`
* `/$documentation`
    * `/openapi2.{yml, json}`
    * `/openapi3.{yml, json}`

{% capture better_ways %}
There may be better ways to represent some of the data in this API. However, the model has been chosen to better demonstrate
the features of the UAPI Runtime.
{% endcapture %}
{% include callouts/demo.html content=better_ways %}

## Library Backend

We'll be providing a pre-build application layer for you, so you can concentrate on implementing the API layer. You'll get
instructions on how to include it later.

TODO: Details on application layer.
