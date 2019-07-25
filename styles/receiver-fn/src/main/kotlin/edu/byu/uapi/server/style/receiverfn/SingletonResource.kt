package edu.byu.uapi.server.style.receiverfn


import edu.byu.uapi.server.style.receiverfn.requests.resource.singleton.CanView
import edu.byu.uapi.server.style.receiverfn.requests.resource.singleton.CreateOrUpdate
import edu.byu.uapi.server.style.receiverfn.requests.resource.singleton.DeleteRequest
import edu.byu.uapi.server.style.receiverfn.requests.resource.singleton.ExecuteUpdate
import edu.byu.uapi.server.style.receiverfn.requests.resource.singleton.Load
import edu.byu.uapi.server.style.receiverfn.requests.resource.singleton.UpdateRequest

interface SingletonResource<Caller, Model> :
    Resource<Caller, Model, Load<Caller>, CanView<Caller, Model>>,
    SingletonEndpoint<Caller, Nothing, Model, Load<Caller>, CanView<Caller, Model>> {

}

typealias CreateOrUpdatableSingletonResource<Caller, Model, Input> = CreateOrUpdateMutation<Caller, Nothing, Model, Input, CreateOrUpdate<Caller, Model, Input>, CreateOrUpdate.ExecuteCreate<Caller, Input>, CreateOrUpdate.ExecuteUpdate<Caller, Model, Input>>
typealias UpdatableSingletonResource<Caller, Model, Input> = UpdateMutation<Caller, Nothing, Model, Input, UpdateRequest<Caller, Model, Input>, ExecuteUpdate<Caller, Model, Input>>
typealias DeletableSingletonResource<Caller, Model> = DeleteMutation<Caller, Nothing, Model, DeleteRequest<Caller, Model>>
