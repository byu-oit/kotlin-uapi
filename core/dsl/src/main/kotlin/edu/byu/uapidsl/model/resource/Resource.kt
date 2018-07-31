package edu.byu.uapidsl.model.resource

import edu.byu.uapidsl.model.ResponseModel
import edu.byu.uapidsl.model.resource.identified.AuthorizedContext
import edu.byu.uapidsl.model.validation.Validating
import kotlin.reflect.KClass

interface Resource<
    Auth : Any,
    Model : Any,
    ModelContext : ResourceModelContext<Model>,
    OptionalModelContext : ResourceOptionalModelContext<Model>,
    Operations : ResourceOperations<Auth, Model, ModelContext, OptionalModelContext>
    > {
    val type: KClass<Model>
    val responseModel: ResponseModel<Model>
    val name: String
    val example: Model
    val operations: Operations
}

interface ResourceOperations<
    Auth : Any,
    Model : Any,
    ModelContext : ResourceModelContext<Model>,
    OptionalModelContext : ResourceOptionalModelContext<Model>> {

    val read: ResourceReadOperation<Auth, Model, ModelContext, *>

    val update: ResourceUpdateOperation<Auth, Model, *, ModelContext>?
    val createOrUpdate: ResourceCreateOrUpdateOperation<Auth, Model, *, ModelContext, OptionalModelContext>?

    val delete: ResourceDeleteOperation<Auth, Model, ModelContext>?
}

interface ResourceModelContext<Model : Any> {
    val model: Model
}

interface ResourceOptionalModelContext<Model : Any> {
    val model: Model?
}

typealias Authorizer<Context> = Context.() -> Boolean
typealias Handler<Context> = Context.() -> Unit
typealias Validator<Context> = Context.() -> Unit
typealias Loader<Context, Model> = Context.() -> Model?
typealias MutationPossibleChecker<Context> = Context.() -> Boolean


interface ResourceLoadContext<Auth : Any> : AuthorizedContext<Auth>

interface ResourceReadContext<
    Auth : Any, Model : Any, Resource : ResourceModelContext<Model>>
    : AuthorizedContext<Auth> {
    val resource: Resource

    data class Default<
        AuthContext : Any,
        Model : Any,
        Resource : ResourceModelContext<Model>
        >(
        override val authContext: AuthContext,
        override val resource: Resource
    ) : ResourceReadContext<AuthContext, Model, Resource>

}

interface ResourceReadOperation<
    Auth : Any,
    Model : Any,
    Resource : ResourceModelContext<Model>,
    LoadContext : ResourceLoadContext<Auth>
    > {
    val authorized: Authorizer<ResourceReadContext<Auth, Model, Resource>>
    val handle: Loader<LoadContext, Model>
}

interface MutatingOperation<
    Model : Any,
    Resource : ResourceModelContext<Model>> {

    val possible: MutationPossibleChecker<ResourceMutationPossibleContext<Model, Resource>>
}

interface ResourceUpdateOperation<
    Auth : Any,
    Model : Any,
    Input : Any,
    Resource : ResourceModelContext<Model>>
    : MutatingOperation<Model, Resource> {
    val authorized: Authorizer<ResourceUpdateContext<Auth, Model, Input, Resource>>
    val handle: Handler<ResourceUpdateContext<Auth, Model, Input, Resource>>
    val validate: Validator<ResourceUpdateValidationContext<Auth, Model, Input, Resource>>
}

interface ResourceDeleteOperation<
    Auth : Any,
    Model : Any,
    Resource : ResourceModelContext<Model>>
    : MutatingOperation<Model, Resource> {
    val authorized: Authorizer<ResourceDeleteContext<Auth, Model, Resource>>
    val handle: Handler<ResourceDeleteContext<Auth, Model, Resource>>
}

interface ResourceCreateOrUpdateOperation<
    Auth : Any,
    Model : Any,
    Input : Any,
    Resource : ResourceModelContext<Model>,
    OptionalResource : ResourceOptionalModelContext<Model>>
    : MutatingOperation<Model, Resource>{
    val authorized: Authorizer<ResourceCreateOrUpdateContext<Auth, Model, Input, OptionalResource>>
    val handle: Handler<ResourceCreateOrUpdateContext<Auth, Model, Input, OptionalResource>>
    val validate: Validator<ResourceCreateOrUpdateValidationContext<Auth, Model, Input, OptionalResource>>
}

interface ResourceUpdateContext<
    Auth : Any,
    Model : Any,
    Input : Any,
    Resource : ResourceModelContext<Model>> : AuthorizedContext<Auth> {
    val resource: Resource
    val input: Input

    data class Default<
        Auth : Any,
        Model : Any,
        Input : Any,
        Resource : ResourceModelContext<Model>>(
        override val authContext: Auth,
        override val resource: Resource,
        override val input: Input

    ) : ResourceUpdateContext<Auth, Model, Input, Resource>
}

interface ResourceCreateOrUpdateContext<
    Auth : Any,
    Model : Any,
    Input : Any,
    OptionalResource : ResourceOptionalModelContext<Model>> : AuthorizedContext<Auth> {
    val resource: OptionalResource
    val input: Input

    data class Default<
        Auth : Any,
        Model : Any,
        Input : Any,
        Resource : ResourceOptionalModelContext<Model>>(
        override val authContext: Auth,
        override val resource: Resource,
        override val input: Input
    ) : ResourceCreateOrUpdateContext<Auth, Model, Input, Resource>
}


interface ResourceUpdateValidationContext<
    Auth : Any,
    Model : Any,
    Input : Any,
    Resource : ResourceModelContext<Model>>
    : ResourceUpdateContext<Auth, Model, Input, Resource>, Validating {

    data class Default<
        Auth : Any,
        Model : Any,
        Input : Any,
        Resource : ResourceModelContext<Model>>(
        override val authContext: Auth,
        override val resource: Resource,
        override val input: Input,
        private val validator: Validating
    ) : ResourceUpdateValidationContext<Auth, Model, Input, Resource>, Validating by validator

}

interface ResourceMutationPossibleContext<
    Model : Any,
    Resource : ResourceModelContext<Model>> {

    val resource: Resource

    data class Default<
        Model : Any,
        Resource : ResourceModelContext<Model>>(
        override val resource: Resource
    ) : ResourceMutationPossibleContext<Model, Resource>
}

interface ResourceCreateOrUpdateValidationContext<
    Auth : Any,
    Model : Any,
    Input : Any,
    Resource : ResourceOptionalModelContext<Model>>
    : ResourceCreateOrUpdateContext<Auth, Model, Input, Resource>, Validating {

    data class Default<
        Auth : Any,
        Model : Any,
        Input : Any,
        Resource : ResourceOptionalModelContext<Model>>(
        override val authContext: Auth,
        override val resource: Resource,
        override val input: Input,
        private val validator: Validating
    ) : ResourceCreateOrUpdateValidationContext<Auth, Model, Input, Resource>, Validating by validator

}

interface ResourceDeleteContext<
    Auth : Any,
    Model : Any,
    Resource : ResourceModelContext<Model>> : AuthorizedContext<Auth> {
    val resource: Resource

    data class Default<
        Auth : Any,
        Model : Any,
        Resource : ResourceModelContext<Model>>(
        override val authContext: Auth,
        override val resource: Resource
    ) : ResourceDeleteContext<Auth, Model, Resource>

}



