package edu.byu.uapidsl.model.resource.identified

import edu.byu.uapidsl.dsl.PagingParams
import edu.byu.uapidsl.model.validation.Validating

interface AuthorizedContext<Auth> {
    val authContext: Auth
}

interface ListContext<Auth, Filters> : AuthorizedContext<Auth> {
    val filters: Filters
}

interface PagedListContext<Auth, Filters> : ListContext<Auth, Filters> {
    val paging: PagingParams
}

interface IdentifiedContext<Auth, Id> : AuthorizedContext<Auth> {
    val id: Id
}

interface IdentifiedResourceContext<Auth, Id, Model> : IdentifiedContext<Auth, Id> {
    val resource: Model
}

interface CreateContext<Auth, Input> : AuthorizedContext<Auth>, InputContext<Input> {
    data class Default<Auth, Input>(
        override val authContext: Auth,
        override val input: Input
    ) : CreateContext<Auth, Input>
}

interface CreateValidationContext<Auth, Input> : CreateContext<Auth, Input>, Validating {
    data class Default<Auth, Input>(
        override val authContext: Auth,
        override val input: Input,
        private val validator: Validating
    ) : CreateValidationContext<Auth, Input>, Validating by validator
}

interface CreateWithIdContext<Auth: Any, Id: Any, Input: Any>: IdentifiedContext<Auth, Id>, InputContext<Input> {
    data class Default<Auth: Any, Id: Any, Input: Any>(
        override val authContext: Auth,
        override val id: Id,
        override val input: Input
    ): CreateWithIdContext<Auth, Id, Input>
}

interface ReadLoadContext<Auth, Id> : IdentifiedContext<Auth, Id>
interface ReadContext<Auth, Id, Model> : IdentifiedResourceContext<Auth, Id, Model>
interface InputContext<Input> {
    val input: Input
}

interface UpdateContext<Auth, Id, Model, Input> : IdentifiedResourceContext<Auth, Id, Model>, InputContext<Input>
interface CreateOrUpdateContext<Auth, Id, Model, Input> : IdentifiedContext<Auth, Id>, InputContext<Input> {
    val resource: Model?
}

interface DeleteContext<Auth, Id, Model> : IdentifiedResourceContext<Auth, Id, Model>

