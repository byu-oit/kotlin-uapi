package edu.byu.uapi.server.style.receiverfn

import edu.byu.uapi.server.style.receiverfn.requests.MutationRequest

interface Mutation {

}

interface CreateMutation<
    Caller,
    out Parent,
    Model,
    Input,
    ReqType : MutationRequest.Create<Caller, Parent, Input>,
    ExecuteType : MutationRequest.Create.Execute<Caller, Parent, Input>
    > : Mutation {

    suspend fun ReqType.canUserCreate(): Boolean
    suspend fun ExecuteType.create(): CreateResult<Model>

}

class CreateResult<Model>

interface UpdateMutation<
    Caller,
    out Parent,
    Model,
    Input,
    ReqType : MutationRequest.Update<Caller, Parent, Model, Input>,
    ExecuteType : MutationRequest.Update.Execute<Caller, Parent, Model, Input>
    > : Mutation {

    suspend fun ReqType.canUserUpdate(): Boolean
    suspend fun ReqType.canBeUpdated(): Boolean
    suspend fun ExecuteType.update(): UpdateResult<Model>

}

class UpdateResult<Model>


interface DeleteMutation<
    Caller,
    out Parent,
    Model,
    ReqType : MutationRequest.Delete<Caller, Parent, Model>
    > : Mutation {

    suspend fun ReqType.canUserDelete(): Boolean
    suspend fun ReqType.canBeDeleted(): Boolean
    suspend fun ReqType.delete(): DeleteResult

}

class DeleteResult

interface CreateOrUpdateMutation<
    Caller,
    out Parent,
    Model,
    Input,
    ReqType : MutationRequest.CreateOrUpdate<Caller, Parent, Model, Input>,
    ExecuteCreate : MutationRequest.CreateOrUpdate.ExecuteCreate<Caller, Parent, Input>,
    ExecuteUpdate : MutationRequest.CreateOrUpdate.ExecuteUpdate<Caller, Parent, Model, Input>
    > : Mutation {

    suspend fun ReqType.authorizeCreateOrUpdate()
    suspend fun ExecuteCreate.create(): CreateResult<Model>

    suspend fun ExecuteUpdate.update(): UpdateResult<Model>
}
