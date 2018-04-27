package edu.byu.uapidsl.dsl.subresource.list

import edu.byu.uapidsl.dsl.PagingParams
import edu.byu.uapidsl.dsl.CollectionWithTotal
import edu.byu.uapidsl.UApiMarker


@UApiMarker
class SubOperationsInit<AuthContext, ParentId, ParentModel, SubId, SubModel> {
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
            handler: SubListHandler<AuthContext, ParentId, ParentModel, SubId, FilterType>
    ) {

    }

    inline fun <reified FilterType> listPaged(
        init: SubPagedCollectionInit<AuthContext, ParentId, ParentModel, SubId, FilterType>.() -> Unit
    ) {

    }

}

@UApiMarker
class SubReadInit<AuthContext, ParentId, ParentModel, SubId, SubModel> {
    fun authorization(auth: SubReadAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel>) {

    }

    fun handle(handler: SubReadHandler<AuthContext, ParentId, ParentModel, SubId, SubModel>) {

    }

}

@UApiMarker
class SubPagedCollectionInit<AuthContext, ParentId, ParentModel, SubId, FilterType> {
    var defaultSize: Int = Int.MAX_VALUE
    var maxSize: Int = Int.MAX_VALUE

    fun handle(handler: SubPagedListHandler<AuthContext, ParentId, ParentModel, SubId, FilterType>) {

    }
}

typealias SubListHandler<AuthContext, ParentId, ParentModel, SubId, FilterType> =
        (SubListContext<AuthContext, ParentId, ParentModel, FilterType>) -> Collection<SubId>

interface SubListContext<AuthContext, ParentId, ParentModel, FilterType> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val filters: FilterType
}

typealias SubPagedListHandler<AuthContext, ParentId, ParentModel, SubId, FilterType> =
        (SubPagedListContext<AuthContext, ParentId, ParentModel, FilterType>) -> CollectionWithTotal<SubId>

interface SubPagedListContext<AuthContext, ParentId, ParentModel, FilterType> {
    val authContext: AuthContext
    val parentId: ParentId
    val parentModel: ParentModel
    val filters: FilterType
    val paging: PagingParams
}


@UApiMarker
class SubCreateInit<AuthContext, ParentId, ParentModel, SubId, CreateModel> {
    fun authorization(auth: SubCreateAuthorizer<AuthContext, ParentId, ParentModel, CreateModel>) {

    }

    fun handle(handler: SubCreateHandler<AuthContext, ParentId, ParentModel, SubId, CreateModel>) {

    }
}

@UApiMarker
class SubUpdateInit<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel> {
    fun authorization(auth: SubUpdateAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel>) {

    }

    fun handle(handler: SubUpdateHandler<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel>) {

    }
}

@UApiMarker
class SubCreateOrUpdateInit<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel> {
    fun authorization(auth: SubCreateOrUpdateAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel>) {

    }

    fun handle(handler: SubCreateOrUpdateHandler<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel>) {

    }
}

@UApiMarker
class SubDeleteInit<AuthContext, ParentId, ParentModel, SubId, SubModel> {
    fun authorization(auth: SubDeleteAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel>) {

    }

    fun handle(handler: SubDeleteHandler<AuthContext, ParentId, ParentModel, SubId, SubModel>) {

    }
}



typealias SubCreateHandler<AuthContext, ParentId, ParentModel, SubId, CreateModel> =
        (context: SubCreateContext<AuthContext, ParentId, ParentModel, CreateModel>) -> SubId

typealias SubUpdateHandler<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel> =
        (context: SubUpdateContext<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel>) -> Unit

typealias SubCreateOrUpdateHandler<AuthContext, ParentId, ParentModel, SubId, SubModel, InputModel> =
        (context: SubCreateOrUpdateContext<AuthContext, ParentId, ParentModel, SubId, SubModel, InputModel>) -> Unit

typealias SubDeleteHandler<AuthContext, ParentId, ParentModel, SubId, SubModel> =
        (context: SubDeleteContext<AuthContext, ParentId, ParentModel, SubId, SubModel>) -> Unit

typealias SubReadHandler<AuthContext, ParentId, ParentModel, SubId, SubModel> =
        (context: SubReadLoadContext<AuthContext, ParentId, ParentModel, SubId>) -> SubModel?


typealias SubReadAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel> =
        (context: SubReadContext<AuthContext, ParentId, ParentModel, SubId, SubModel>) -> Boolean

typealias SubCreateAuthorizer<AuthContext, ParentId, ParentModel, CreateModel> =
        (context: SubCreateContext<AuthContext, ParentId, ParentModel, CreateModel>) -> Boolean

typealias SubUpdateAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel> =
        (context: SubUpdateContext<AuthContext, ParentId, ParentModel, SubId, SubModel, UpdateModel>) -> Boolean

typealias SubCreateOrUpdateAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel, InputModel> =
        (context: SubCreateOrUpdateContext<AuthContext, ParentId, ParentModel, SubId, SubModel, InputModel>) -> Boolean

typealias SubDeleteAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel> =
        (context: SubDeleteContext<AuthContext, ParentId, ParentModel, SubId, SubModel>) -> Boolean



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
