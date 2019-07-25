package edu.byu.uapi.server.style.receiverfn

import edu.byu.uapi.server.style.receiverfn.requests.subresource.singleton.CanView
import edu.byu.uapi.server.style.receiverfn.requests.subresource.singleton.CreateOrUpdate
import edu.byu.uapi.server.style.receiverfn.requests.subresource.singleton.DeleteRequest
import edu.byu.uapi.server.style.receiverfn.requests.subresource.singleton.ExecuteUpdate
import edu.byu.uapi.server.style.receiverfn.requests.subresource.singleton.Load
import edu.byu.uapi.server.style.receiverfn.requests.subresource.singleton.UpdateRequest

interface SingletonSubresource<Caller, Parent, Id : Any, Model : Any> :
    Subresource<Caller, Parent, Model, Load<Caller, Parent>, CanView<Caller, Parent, Model>>,
    SingletonEndpoint<Caller, Parent, Model, Load<Caller, Parent>, CanView<Caller, Parent, Model>> {

}

typealias CreateOrUpdatableSingletonSubresource<Caller, Parent, Model, Input> =
    CreateOrUpdateMutation<Caller, Parent, Model, Input, CreateOrUpdate<Caller, Parent, Model, Input>, CreateOrUpdate.ExecuteCreate<Caller, Parent, Input>, CreateOrUpdate.ExecuteUpdate<Caller, Parent, Model, Input>>

typealias UpdatableSingletonSubresource<Caller, Parent, Model, Input> =
    UpdateMutation<Caller, Parent, Model, Input, UpdateRequest<Caller, Parent, Model, Input>, ExecuteUpdate<Caller, Parent, Model, Input>>

typealias DeletableSingletonSubresource<Caller, Parent, Model> =
    DeleteMutation<Caller, Parent, Model, DeleteRequest<Caller, Parent, Model>>
