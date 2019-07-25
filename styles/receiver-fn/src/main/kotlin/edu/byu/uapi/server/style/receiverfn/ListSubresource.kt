package edu.byu.uapi.server.style.receiverfn

import edu.byu.uapi.server.style.receiverfn.requests.subresource.list.CanView
import edu.byu.uapi.server.style.receiverfn.requests.subresource.list.CreateOrUpdate
import edu.byu.uapi.server.style.receiverfn.requests.subresource.list.CreateRequest
import edu.byu.uapi.server.style.receiverfn.requests.subresource.list.DeleteRequest
import edu.byu.uapi.server.style.receiverfn.requests.subresource.list.ExecuteCreate
import edu.byu.uapi.server.style.receiverfn.requests.subresource.list.ExecuteUpdate
import edu.byu.uapi.server.style.receiverfn.requests.subresource.list.ListRequest
import edu.byu.uapi.server.style.receiverfn.requests.subresource.list.Load
import edu.byu.uapi.server.style.receiverfn.requests.subresource.list.UpdateRequest

interface ListSubresource<Caller, Parent, Id : Any, Model : Any, ListParams : Any> :
    Subresource<Caller, Parent, Model, Load<Caller, Parent, Id>, CanView<Caller, Parent, Id, Model>>,
    ListEndpoint<Caller, Parent, Id, Model, ListParams, Load<Caller, Parent, Id>, CanView<Caller, Parent, Id, Model>, ListRequest<Caller, Parent, ListParams>> {

}

typealias CreatableListSubresource<Caller, Parent, Id, Model, Input> =
    CreateMutation<Caller, Parent, Model, Input, CreateRequest<Caller, Parent, Input>, ExecuteCreate<Caller, Parent, Input>>

typealias UpdatableListSubresource<Caller, Parent, Id, Model, Input> =
    UpdateMutation<Caller, Parent, Model, Input, UpdateRequest<Caller, Parent, Id, Model, Input>, ExecuteUpdate<Caller, Parent, Id, Model, Input>>

typealias DeletableListSubresource<Caller, Parent, Id, Model> =
    DeleteMutation<Caller, Parent, Model, DeleteRequest<Caller, Parent, Id, Model>>

typealias CreatableWithIdListSubresource<Caller, Parent, Id, Model, Input> =
    CreateOrUpdateMutation<Caller, Parent, Model, Input, CreateOrUpdate<Caller, Parent, Id, Model, Input>, CreateOrUpdate.ExecuteCreate<Caller, Parent, Id, Input>, CreateOrUpdate.ExecuteUpdate<Caller, Parent, Id, Model, Input>>

