package edu.byu.uapidsl.model.resource.singleton

import edu.byu.uapidsl.model.ResponseModel
import edu.byu.uapidsl.model.resource.*
import edu.byu.uapidsl.model.resource.identified.AuthorizedContext
import kotlin.reflect.KClass

class SingletonResource<
    AuthContext : Any,
    Model : Any>(
    override val type: KClass<Model>,
    override val responseModel: ResponseModel<Model>,
    override val name: String,
    override val example: Model,
    override val operations: SingletonResourceOperations<AuthContext, Model>
) : Resource<
    AuthContext,
    Model,
    SingletonResourceModelContext<Model>,
    SingletonResourceOptionalModelContext<Model>,
    SingletonResourceOperations<AuthContext, Model>
    >

data class SingletonResourceModelContext<Model : Any>(
    override val model: Model
) : ResourceModelContext<Model>

data class SingletonResourceOptionalModelContext<Model : Any>(
    override val model: Model?
) : ResourceOptionalModelContext<Model>

class SingletonResourceOperations<
    AuthContext : Any,
    Model : Any
    >(
    override val read: ResourceReadOperation<AuthContext, Model, SingletonResourceModelContext<Model>, *>,
    override val update: ResourceUpdateOperation<AuthContext, Model, *, SingletonResourceModelContext<Model>>?,
    override val createOrUpdate: ResourceCreateOrUpdateOperation<AuthContext, Model, *, SingletonResourceOptionalModelContext<Model>>?,
    override val delete: ResourceDeleteOperation<AuthContext, Model, SingletonResourceModelContext<Model>>?
) : ResourceOperations<AuthContext, Model,
    SingletonResourceModelContext<Model>,
    SingletonResourceOptionalModelContext<Model>> {
}


interface SingletonResourceReadLoadContext<AuthContext : Any> : ResourceLoadContext<AuthContext>

interface SingletonResourceReadContext<AuthContext, Model : Any> :
    AuthorizedContext<AuthContext> {
    val resource: SingletonResourceModelContext<Model>
}

class SingletonResourceReadOperation<AuthContext : Any, Model : Any>(
    override val authorized: Authorizer<ResourceReadContext<AuthContext, Model, SingletonResourceModelContext<Model>>>,
    override val handle: Loader<SingletonResourceReadLoadContext<AuthContext>, Model>
) : ResourceReadOperation<
    AuthContext,
    Model,
    SingletonResourceModelContext<Model>,
    SingletonResourceReadLoadContext<AuthContext>> {

}

class SingletonResourceUpdateOperation<AuthContext : Any, Model : Any, Input : Any>(
    override val authorized: Authorizer<ResourceUpdateContext<AuthContext, Model, Input, SingletonResourceModelContext<Model>>>,
    override val handle: Handler<ResourceUpdateContext<AuthContext, Model, Input, SingletonResourceModelContext<Model>>>,
    override val validate: Validator<ResourceUpdateValidationContext<AuthContext, Model, Input, SingletonResourceModelContext<Model>>>
) : ResourceUpdateOperation<
    AuthContext,
    Model,
    Input,
    SingletonResourceModelContext<Model>>

