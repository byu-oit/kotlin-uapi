# Implementation Styles

There are two styles that can be followed when implementing a UAPI resource: Interface or DSL. These styles can
be mixed-and-matched.


# Interface Style

With this style, you must implement a series of interfaces, then register them with a UAPI runtime instance.

Here is a very basic example, which only supports fetching a single instance of a resource from an ID:

```kotlin

class FooResource: IdentifiedResource<
    MyUserContext,  // This contains information about the user. See TODO for more information.
    String, // This is the type of the ID for a Foo. See TODO for more.
    FooDTO // This is the type we will actually send back to the consumer. See TODO for more.
    > {
    
    override val idType = String::class
    override val modelType = FooDTO::class
    
    override fun loadModel(userContext: MyUserContext, id: String): FooDTO? {
      TODO("Load a Foo from ID. If there isn't one matching the provided ID, return null.")
    }
    
    override fun canUserViewModel(userContext: MyUserContext, id: String, model: FooDTO): Boolean {
      TODO("Check the current user's permissions and return 'true' if they can see this record.")
    }
    
    override fun idFromModel(model: FooDTO): String {
        return model.fooId
    }
    
}

```

As you can see, you must provide a class for the ID type and model type and implement function that
load a model from an ID, check if a user can see a given model, and extract an ID from the model.

Given these methods, the runtime is able to respond properly to requests to get a resource, including
checking authorizations and constructing metadata and links.

* If `loadModel` returns null, an HTTP 404 will be returned, in compliance with the UAPI Specification.
* If `canUserViewModel` returns false, an HTTP 403 will be returned, in compliance with the UAPI Specification.

## Adding More Operations

Obviously, an API that can only get individual records is not very useful.  In order to add more functionality,
you need to implement an interface for each possible operation.  The interfaces you can implement are:

Interface Name | HTTP Route | Description
---------------|------------|-------------
Creatable      | POST /foos | Creates a new record
Updatable      | PUT /foos/{id} | Update a record
CreatableWithId | PUT /foos/{id} | Creates a new record with the given ID if one doesn't exist
Deletable      | DELETE /foos/{id} | Deletes a record
Listable       | GET /foos | Lists all records
PagedListable  | GET /foos | Lists all records, broken into pages

### Creatable

Given an input model, allows an authorized caller to create a new record. You must provide the type of the input model,
authorization rules, and validation rules, as well as specifying how to actually save the new record.

```kotlin

class FooResource: IdentifiedResource<MyUserContext, String, FooDTO>,
                   IdentifiedResource.Creatable<MyUserContext, String, FooDTO, NewFooJson> {
                     
    // Other resource methods
    
    override val createInput = NewFooJson::class
    
    override fun canUserCreate(userContext: MyUserContext): Boolean {
      TODO("Check if a user can create a record. Return 'true' if they can.")
    }
    
    override fun validateCreateInput(userContext: MyUserContext, input: NewFooJson, validation: Validating) {
      TODO("Use 'validation' to make assertions about the validity of the data in 'input'.")
    }
    
    override fun handleCreate(userContext: UserContext, input: NewFooJson): String {
      TODO("Actually create the record and return the new record's ID.")
    }
                     
}
    
```

* If `canUserCreate` returns false, an HTTP 403 will be returned.
* If any failed assertions are recorded in `validateCreateInput`, an HTTP 400 will be returned.  See TODO for information about validation.

### Updatable

Given an input model, allows an authorized caller to update a record. The runtime does not express any opinions about how
updates are to be handled, beyond enforcing authorization and validation.

```kotlin

class FooResource: IdentifiedResource<MyUserContext, String, FooDTO>,
                   IdentifiedResource.Updatable<MyUserContext, String, FooDTO, UpdateFooJson> {
                     
    // Other resource methods
    
    override val updateInput = UpdateFooJson::class
    
    override fun canUserUpdate(userContext: MyUserContext, id: String, model: FooDTO): Boolean {
      TODO("Check if a user can update this record. Return 'true' if they can.")
    }
    
    override fun canBeUpdated(id: String, model: Model): Boolean {
      TODO("Return 'true' if the provided record can be updated at all, regardless of user authorizations.")
    }
    
    override fun validateUpdateInput(userContext: MyUserContext, id: String, model: FooDTO, input: UpdateFooJson, validation: Validating) {
      TODO("Use 'validation' to make assertions about the validity of the data in 'input'.")
    }
    
    override fun handleUpdate(userContext: UserContext, id: String, model: FooDTO, input: NewFooJson) {
      TODO("Update the actual record.")
    }
                     
}
    
```

* If no record exists with the provided ID (as determined by calling `loadModel`), an HTTP 404 will be returned.
* If `canUserUpdate` returns false, an HTTP 403 will be returned.
* If `canBeUpdated` returns false, an HTTP 409 will be returned.
* If any failed assertions are recorded in `validateUpdateInput`, an HTTP 400 will be returned.  See TODO for information about validation.

### CreatableWithId

Given an input model, allows an authorized caller to create a record with the given ID. This complements Createable
and Updatable.

This interface is used when you wish to provide an idempotent create/update endpoint, wherein, if an Update
request is made a non-existent record, a new one with the specified ID will be created.

**TODO:** This interface needs some serious thought. It should probably extend Updatable so that the two share an input type.

### Deletable

Given an ID, allows an authorized caller to delete a record.

```kotlin

class FooResource: IdentifiedResource<MyUserContext, String, FooDTO>,
                   IdentifiedResource.Deletable<MyUserContext, String, FooDTO> {
                     
    // Other resource methods
    
    override fun canUserDelete(userContext: MyUserContext, id: String, model: FooDTO): Boolean {
      TODO("Check if a user can delete this record. Return 'true' if they can.")
    }
    
    override fun canBeDeleted(id: String, model: Model): Boolean {
      TODO("Return 'true' if the provided record can be deleted at all, regardless of user authorizations.")
    }
    
    override fun handleDelete(userContext: UserContext, id: String, model: FooDTO) {
      TODO("Actually delete the record.")
    }
                     
}
    
```

### Listable

Returns a list of all records in a collection. Optionally allows for filtering. The runtime makes no assumptions about
how filtering is implemented; you must just provide a class with fields for all possible filters.

The runtime will automatically construct links and validate view permissions for all returned records.

```kotlin

class FooResource: IdentifiedResource<MyUserContext, String, FooDTO>,
                   IdentifiedResource.Listable<MyUserContext, String, FooDTO, FooFilters> {
                     
    // Other resource methods
    
    override val filterType = FooFilters::class
    
    override fun list(userContext: MyUserContext, filters: FooFilters): Collection<FooDTO> {
      TODO("Use the provided filters to get a list of all Foos")
    }
                     
}

```

**TODO:** Should we make filters nullable, and pass in null if no matching filter parameters were passed?

### PagedListable

Returns a paged list of all records in a collection. Optionally allows for filtering. The runtime makes no assumptions about
how filtering is implemented; you must just provide a class with fields for all possible filters.

The runtime will automatically construct links and validate view permissions for all returned records.

**WARNING** If you do not filter the results returned by `list` to just those the specified user can see, the caller
may see odd results and be able to deduce the existence of records they are not allowed to see. This can lead to major
security/privacy issues, so be sure to implement `list` correctly!

```kotlin

class FooResource: IdentifiedResource<MyUserContext, String, FooDTO>,
                   IdentifiedResource.PagedListable<MyUserContext, String, FooDTO, FooFilters> {
                     
    // Other resource methods
    
    override val filterType = FooFilters::class
    
    override fun list(userContext: MyUserContext, filters: FooFilters, paging: PagingParams): CollectionWithTotal<FooDTO> {
      TODO("Use the provided filters to get a list of all Foos")
    }
                     
}

```

**TODO:** Should we make filters nullable, and pass in null if no matching filter parameters were passed?

*Note:* `list` returns a special type, `CollectionWithTotal`.  This wraps any collection and includes the *total number of records*
that match the specified filters, not just the number that fit into the requested page.

## Re-usable Operations

It may be desirable to have reusable implementations of these operations.  For example, your API may have a 'foos' resource
and a 'bars' resource, and you have a common way to delete both a Foo and a Bar. To reuse this code, you could implement
the `Deletable` interface in both of your 

# TODO: DSL Style

