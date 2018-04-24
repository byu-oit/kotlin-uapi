package edu.byu.kotlin.uapidsl


class OperationsInit<AuthContext, IdType, ResourceModel> {
    inline fun <reified CreateModel> create(init: CreateInit<AuthContext, IdType, CreateModel>.() -> Unit) {

    }

    inline fun <reified UpdateModel> update(init: UpdateInit<AuthContext, IdType, ResourceModel, UpdateModel>.() -> Unit) {

    }

    inline fun <reified InputModel> createOrUpdate(init: CreateOrUpdateInit<AuthContext, IdType, ResourceModel, InputModel>.() -> Unit) {

    }

    inline fun delete(init: DeleteInit<AuthContext, IdType, ResourceModel>.() -> Unit) {

    }

    inline fun read(init: ReadInit<AuthContext, IdType, ResourceModel>.() -> Unit) {

    }

}

class ReadInit<AuthContext, IdType, ResourceModel> {
    fun authorization(auth: ReadAuthorizer<AuthContext, IdType, ResourceModel>) {

    }

    fun handle(handler: ReadHandler<AuthContext, IdType, ResourceModel>) {

    }

    inline fun <reified FilterType> collection(
            handler: ListHandler<AuthContext, IdType, FilterType>
    ) {

    }

    inline fun <reified FilterType> pagedCollection(
        init: PagedCollectionInit<AuthContext, IdType, FilterType>.() -> Unit
    ) {

    }

}

class PagedCollectionInit<AuthContext, IdType, FilterType> {
    var defaultSize: Int = Int.MAX_VALUE
    var maxSize: Int = Int.MAX_VALUE

    fun handle(handler: PagedListHandler<AuthContext, IdType, FilterType>) {

    }
}

typealias ListHandler<AuthContext, IdType, FilterType> =
        (ListContext<AuthContext, FilterType>) -> Sequence<IdType>

interface ListContext<AuthContext, FilterType> {
    val authContext: AuthContext
    val filters: FilterType
}

typealias PagedListHandler<AuthContext, IdType, FilterType> =
        (PagedListContext<AuthContext, FilterType>) -> SequenceWithTotal<IdType>

interface PagedListContext<AuthContext, FilterType> {
    val authContext: AuthContext
    val filters: FilterType
    val paging: PagingParams
}


class CreateInit<AuthContext, IdType, CreateModel> {
    fun authorization(auth: CreateAuthorizer<AuthContext, CreateModel>) {

    }

    fun handle(handler: CreateHandler<AuthContext, IdType, CreateModel>) {

    }
}

class UpdateInit<AuthContext, IdType, ResourceModel, UpdateModel> {
    fun authorization(auth: UpdateAuthorizer<AuthContext, IdType, ResourceModel, UpdateModel>) {

    }

    fun handle(handler: UpdateHandler<AuthContext, IdType, ResourceModel, UpdateModel>) {

    }
}

class CreateOrUpdateInit<AuthContext, IdType, ResourceModel, UpdateModel> {
    fun authorization(auth: CreateOrUpdateAuthorizer<AuthContext, IdType, ResourceModel, UpdateModel>) {

    }

    fun handle(handler: CreateOrUpdateHandler<AuthContext, IdType, ResourceModel, UpdateModel>) {

    }
}

class DeleteInit<AuthContext, IdType, ResourceModel> {
    fun authorization(auth: DeleteAuthorizer<AuthContext, IdType, ResourceModel>) {

    }

    fun handle(handler: DeleteHandler<AuthContext, IdType, ResourceModel>) {

    }
}



typealias CreateHandler<AuthContext, IdType, CreateModel> =
        (CreateContext<AuthContext, CreateModel>) -> IdType

typealias UpdateHandler<AuthContext, IdType, ResourceModel, UpdateModel> =
        (UpdateContext<AuthContext, IdType, ResourceModel, UpdateModel>) -> Unit

typealias CreateOrUpdateHandler<AuthContext, IdType, ResourceModel, InputModel> =
        (CreateOrUpdateContext<AuthContext, IdType, ResourceModel, InputModel>) -> Unit

typealias DeleteHandler<AuthContext, IdType, ResourceModel> =
        (DeleteContext<AuthContext, IdType, ResourceModel>) -> Unit

typealias ReadHandler<AuthContext, IdType, ResourceModel> =
        (ReadLoadContext<AuthContext, IdType>) -> ResourceModel?


typealias ReadAuthorizer<AuthContext, IdType, ResourceModel> =
        (ReadContext<AuthContext, IdType, ResourceModel>) -> Boolean

typealias CreateAuthorizer<AuthContext, CreateModel> =
        (CreateContext<AuthContext, CreateModel>) -> Boolean

typealias UpdateAuthorizer<AuthContext, IdType, ResourceModel, UpdateModel> =
        (UpdateContext<AuthContext, IdType, ResourceModel, UpdateModel>) -> Boolean

typealias CreateOrUpdateAuthorizer<AuthContext, IdType, ResourceModel, InputModel> =
        (CreateOrUpdateContext<AuthContext, IdType, ResourceModel, InputModel>) -> Boolean

typealias DeleteAuthorizer<AuthContext, IdType, ResourceModel> =
        (DeleteContext<AuthContext, IdType, ResourceModel>) -> Boolean

interface CreateContext<AuthContext, CreateModel> {
    val authContext: AuthContext
    val input: CreateModel
}

interface ReadLoadContext<AuthContext, IdType> {
    val authContext: AuthContext
    val id: IdType
}

interface ReadContext<AuthContext, IdType, ResourceModel> {
    val authContext: AuthContext
    val id: IdType
    val resource: ResourceModel
}

interface UpdateContext<AuthContext, IdType, ResourceModel, UpdateModel> {
    val authContext: AuthContext
    val id: IdType
    val input: UpdateModel
    val resource: ResourceModel
}

interface CreateOrUpdateContext<AuthContext, IdType, ResourceModel, UpdateModel> {
    val authContext: AuthContext
    val id: IdType
    val input: UpdateModel
    val resource: ResourceModel?
}

interface DeleteContext<AuthContext, IdType, ResourceModel> {
    val authContext: AuthContext
    val id: IdType
    val resource: ResourceModel
}
