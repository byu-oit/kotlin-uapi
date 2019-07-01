package edu.byu.uapi.server.subresources.singleton

import edu.byu.uapi.server.response.ResponseField
import edu.byu.uapi.server.response.UAPIResponseInit
import edu.byu.uapi.server.response.uapiResponse
import edu.byu.uapi.server.subresources.Subresource
import edu.byu.uapi.server.subresources.SubresourceRequestContext
import edu.byu.uapi.server.types.CreateResult
import edu.byu.uapi.server.types.DeleteResult
import edu.byu.uapi.server.types.ModelHolder
import edu.byu.uapi.server.types.UpdateResult
import edu.byu.uapi.server.util.extrapolateGenericType
import edu.byu.uapi.spi.validation.ValidationEngine
import edu.byu.uapi.spi.validation.Validator
import edu.byu.uapi.utility.takeIfType
import kotlin.reflect.KClass

interface SingletonSubresource<UserContext : Any, Parent : ModelHolder, Model : Any>: Subresource<UserContext, Parent, Model> {

    val name: String

    fun loadModel(
        requestContext: SubresourceRequestContext,
        userContext: UserContext,
        parent: Parent
    ): Model?

    fun canUserViewModel(
        requestContext: SubresourceRequestContext,
        userContext: UserContext,
        parent: Parent,
        model: Model
    ): Boolean

    val responseFields: List<ResponseField<UserContext, Model, *>>

    val updateMutation: Updatable<UserContext, Parent, Model, *>?
        get() = this.takeIfType()
    val deleteMutation: Deletable<UserContext, Parent, Model>?
        get() = this.takeIfType()

    interface Updatable<UserContext : Any, Parent : ModelHolder, Model : Any, Input : Any> {
        fun canUserUpdate(
            requestContext: SubresourceRequestContext,
            userContext: UserContext,
            parent: Parent,
            model: Model
        ): Boolean

        fun canBeUpdated(
            parent: Parent,
            model: Model
        ): Boolean

        fun handleUpdate(
            requestContext: SubresourceRequestContext,
            userContext: UserContext,
            parent: Parent,
            model: Model,
            input: Input
        ): UpdateResult<Model>

        val updateInput: KClass<Input>
            get() = extrapolateGenericType("Input", Updatable<*, *, *, *>::updateInput)

        fun getUpdateValidator(validationEngine: ValidationEngine): Validator<Input> {
            return validationEngine.validatorFor(updateInput)
        }
    }

    interface Creatable<UserContext : Any, Parent : ModelHolder, Model : Any, Input : Any>
        : Updatable<UserContext, Parent, Model, Input> {

        fun canUserCreate(
            requestContext: SubresourceRequestContext,
            userContext: UserContext,
            parent: Parent
        ): Boolean

        fun handleCreate(
            requestContext: SubresourceRequestContext,
            userContext: UserContext,
            parent: Parent,
            input: Input
        ): CreateResult<Model>
    }

    interface Deletable<UserContext: Any, Parent: ModelHolder, Model: Any> {
        fun canUserDelete(
            requestContext: SubresourceRequestContext,
            userContext: UserContext,
            parent: Parent,
            model: Model
        ): Boolean

        fun canBeDeleted(
            parent: Parent,
            model: Model
        ): Boolean

        fun handleDelete(
            requestContext: SubresourceRequestContext,
            userContext: UserContext,
            parent: Parent,
            model: Model
        ): DeleteResult
    }
}

inline fun <UserContext : Any, Model : Any> SingletonSubresource<UserContext, *, Model>.fields(fn: UAPIResponseInit<UserContext, Model>.() -> Unit)
    : List<ResponseField<UserContext, Model, *>> = uapiResponse(fn)
