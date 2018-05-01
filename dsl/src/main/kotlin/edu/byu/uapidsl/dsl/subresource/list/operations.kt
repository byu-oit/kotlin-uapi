@file:Suppress("unused", "UNUSED_PARAMETER")

package edu.byu.uapidsl.dsl.subresource.list

import edu.byu.uapidsl.DSLInit
import edu.byu.uapidsl.ValidationContext
import edu.byu.uapidsl.dsl.CollectionWithTotal
import edu.byu.uapidsl.dsl.PagingParams

class SubOperationsInit<AuthContext, ParentId, ParentModel, SubId, SubModel>(
    validation: ValidationContext
) : DSLInit(validation) {
    inline fun <reified CreateModel> create(init: SubCreateInit<AuthContext, ParentId, ParentModel, SubId, CreateModel>.() -> Unit) {

    }

    inline fun <reified UpdateModel> update(init: SubUpdateInit<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel>.() -> Unit) {

    }

    inline fun <reified InputModel> createOrUpdate(init: SubCreateOrUpdateInit<AuthContext, ParentId, ParentModel, SubId, SubModel, InputModel>.() -> Unit) {

    }

    inline fun delete(init: SubDeleteInit<AuthContext, ParentId, ParentModel, SubId, SubModel>.() -> Unit) {

    }

    inline fun read(init: SubReadInit<AuthContext, ParentId, ParentModel, SubId, SubModel>.() -> Unit) {

    }

    inline fun <reified FilterType> listSimple(
        init: SubSimpleCollectionInit<AuthContext, ParentId, ParentModel, SubId, SubModel, FilterType>.() -> Unit
    ) {

    }

    inline fun <reified FilterType> listPaged(
        init: SubPagedCollectionInit<AuthContext, ParentId, ParentModel, SubId, SubModel, FilterType>.() -> Unit
    ) {

    }

}

class SubReadInit<AuthContext, ParentId, ParentModel, SubId, SubModel>(
    validation: ValidationContext
) : DSLInit(validation) {
    fun authorization(auth: SubReadAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel>) {

    }

    fun handle(handler: SubReadHandler<AuthContext, ParentId, ParentModel, SubId, SubModel>) {

    }

}

class SubPagedCollectionInit<AuthContext, ParentId, ParentModel, SubId, SubModel, FilterType>(
    validation: ValidationContext
) : DSLInit(validation) {
    var defaultSize: Int = Int.MAX_VALUE
    var maxSize: Int = Int.MAX_VALUE

    fun listIds(handler: SubPagedListHandler<AuthContext, ParentId, ParentModel, FilterType, SubId>) {

    }

    fun listObjects(handler: SubPagedListHandler<AuthContext, ParentId, ParentModel, FilterType, SubModel>) {

    }
}

class SubSimpleCollectionInit<AuthContext, ParentId, ParentModel, FilterType, SubId, SubModel>(
    validation: ValidationContext
) : DSLInit(validation) {

    fun listIds(handler: SubListHandler<AuthContext, ParentId, ParentModel, FilterType, SubId>) {

    }

    fun listObjects(handler: SubListHandler<AuthContext, ParentId, ParentModel, FilterType, SubModel>) {

    }
}

interface SubListContext<AuthContext, ParentId, ParentModel, FilterType> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val filters: FilterType
}

interface SubPagedListContext<AuthContext, ParentId, ParentModel, FilterType> {
    val authContext: AuthContext
    val parentId: ParentId
    val parentModel: ParentModel
    val filters: FilterType
    val paging: PagingParams
}

class SubCreateInit<AuthContext, ParentId, ParentModel, SubId, CreateModel>(
    validation: ValidationContext
) : DSLInit(validation) {
    fun authorization(auth: SubCreateAuthorizer<AuthContext, ParentId, ParentModel, CreateModel>) {

    }

    fun handle(handler: SubCreateHandler<AuthContext, ParentId, ParentModel, SubId, CreateModel>) {

    }
}

class SubUpdateInit<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel>(
    validation: ValidationContext
) : DSLInit(validation) {
    fun authorization(auth: SubUpdateAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel>) {

    }

    fun handle(handler: SubUpdateHandler<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel>) {

    }
}

class SubCreateOrUpdateInit<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel>(
    validation: ValidationContext
) : DSLInit(validation) {
    fun authorization(auth: SubCreateOrUpdateAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel>) {

    }

    fun handle(handler: SubCreateOrUpdateHandler<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel>) {

    }
}

class SubDeleteInit<AuthContext, ParentId, ParentModel, SubId, SubModel>(
    validation: ValidationContext
) : DSLInit(validation) {
    fun authorization(auth: SubDeleteAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel>) {

    }

    fun handle(handler: SubDeleteHandler<AuthContext, ParentId, ParentModel, SubId, SubModel>) {

    }
}

interface SubCreateContext<AuthContext, ParentId, ParentModel, CreateModel> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val input: CreateModel
}

interface SubReadLoadContext<AuthContext, ParentId, ParentModel, SubId> {
    val authContext: AuthContext
    val parentId: ParentId;
    val parent: ParentModel
    val id: SubId
}

interface SubReadContext<AuthContext, ParentId, ParentModel, SubId, SubModel> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val id: SubId
    val resource: SubModel
}

interface SubUpdateContext<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val id: SubId
    val input: UpdateModel
    val resource: SubModel
}

interface SubCreateOrUpdateContext<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val id: SubId
    val input: UpdateModel
    val resource: SubModel?
}

interface SubDeleteContext<AuthContext, ParentId, ParentModel, SubId, SubModel> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val id: SubId
    val resource: SubModel
}

typealias SubListHandler<AuthContext, ParentId, ParentModel, FilterType, ResultType> =
    SubListContext<AuthContext, ParentId, ParentModel, FilterType>.() -> Collection<ResultType>

typealias SubPagedListHandler<AuthContext, ParentId, ParentModel, FilterType, ResultType> =
    SubPagedListContext<AuthContext, ParentId, ParentModel, FilterType>.() -> CollectionWithTotal<ResultType>

typealias SubCreateHandler<AuthContext, ParentId, ParentModel, SubId, CreateModel> =
    SubCreateContext<AuthContext, ParentId, ParentModel, CreateModel>.() -> SubId

typealias SubUpdateHandler<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel> =
    SubUpdateContext<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel>.() -> Unit

typealias SubCreateOrUpdateHandler<AuthContext, ParentId, ParentModel, SubId, SubModel, InputModel> =
    SubCreateOrUpdateContext<AuthContext, ParentId, ParentModel, SubId, SubModel, InputModel>.() -> Unit

typealias SubDeleteHandler<AuthContext, ParentId, ParentModel, SubId, SubModel> =
    SubDeleteContext<AuthContext, ParentId, ParentModel, SubId, SubModel>.() -> Unit

typealias SubReadHandler<AuthContext, ParentId, ParentModel, SubId, SubModel> =
    SubReadLoadContext<AuthContext, ParentId, ParentModel, SubId>.() -> SubModel?


typealias SubReadAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel> =
    SubReadContext<AuthContext, ParentId, ParentModel, SubId, SubModel>.() -> Boolean

typealias SubCreateAuthorizer<AuthContext, ParentId, ParentModel, CreateModel> =
    SubCreateContext<AuthContext, ParentId, ParentModel, CreateModel>.() -> Boolean

typealias SubUpdateAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel> =
    SubUpdateContext<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel>.() -> Boolean

typealias SubCreateOrUpdateAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel, InputModel> =
    SubCreateOrUpdateContext<AuthContext, ParentId, ParentModel, SubId, SubModel, InputModel>.() -> Boolean

typealias SubDeleteAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel> =
    SubDeleteContext<AuthContext, ParentId, ParentModel, SubId, SubModel>.() -> Boolean



