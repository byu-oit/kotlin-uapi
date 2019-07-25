package edu.byu.uapi.server.style.receiverfn

import edu.byu.uapi.server.style.receiverfn.requests.resource.list.CanView
import edu.byu.uapi.server.style.receiverfn.requests.resource.list.CreateOrUpdate
import edu.byu.uapi.server.style.receiverfn.requests.resource.list.CreateRequest
import edu.byu.uapi.server.style.receiverfn.requests.resource.list.DeleteRequest
import edu.byu.uapi.server.style.receiverfn.requests.resource.list.ExecuteCreate
import edu.byu.uapi.server.style.receiverfn.requests.resource.list.ExecuteUpdate
import edu.byu.uapi.server.style.receiverfn.requests.resource.list.ListRequest
import edu.byu.uapi.server.style.receiverfn.requests.resource.list.Load
import edu.byu.uapi.server.style.receiverfn.requests.resource.list.UpdateRequest

interface ListResource<Caller, Id : Any, Model : Any, ListParams> :
    Resource<Caller, Model, Load<Caller, Id>, CanView<Caller, Id, Model>>,
    ListEndpoint<Caller, Nothing, Id, Model, ListParams, Load<Caller, Id>, CanView<Caller, Id, Model>, ListRequest<Caller, ListParams>> {
}

typealias CreatableListResource<Caller, Id, Model, Input> =
    CreateMutation<Caller, Nothing, Model, Input, CreateRequest<Caller, Input>, ExecuteCreate<Caller, Input>>

typealias UpdatableListResource<Caller, Id, Model, Input> =
    UpdateMutation<Caller, Nothing, Model, Input, UpdateRequest<Caller, Id, Model, Input>, ExecuteUpdate<Caller, Id, Model, Input>>

typealias DeletableListResource<Caller, Id, Model> =
    DeleteMutation<Caller, Nothing, Model, DeleteRequest<Caller, Id, Model>>

typealias CreatableWithIdListResource<Caller, Id, Model, Input> =
    CreateOrUpdateMutation<Caller, Nothing, Model, Input, CreateOrUpdate<Caller, Id, Model, Input>, CreateOrUpdate.ExecuteCreate<Caller, Id, Input>, CreateOrUpdate.ExecuteUpdate<Caller, Id, Model, Input>>

