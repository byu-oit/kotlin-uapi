package edu.byu.uapi.server.style.receiverfn.requests.resource.list

import edu.byu.uapi.server.style.receiverfn.requests.ListEndpointRequest

sealed class ListResourceRequest<Caller> : ListEndpointRequest<Caller, Nothing> {
    override val parent: Nothing
        get() = TODO("not implemented for top-level resources")
}

//-----------------------------------------------------------
// Read Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

abstract class Load<Caller, Id> :
    ListResourceRequest<Caller>(),
    ListEndpointRequest.Load<Caller, Nothing, Id>

abstract class CanView<Caller, Id, Model>() :
    ListResourceRequest<Caller>(),
    ListEndpointRequest.CanView<Caller, Nothing, Id, Model>

abstract class ListRequest<Caller, Params> :
    ListResourceRequest<Caller>(),
    ListEndpointRequest.List<Caller, Nothing, Params>

//</editor-fold>

//-----------------------------------------------------------
// Create Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

sealed class CreateRequest<Caller, out Input> :
    ListResourceRequest<Caller>(),
    ListEndpointRequest.Create<Caller, Nothing, Input>

abstract class ExecuteCreate<Caller, Input>(
    override val caller: Caller,
    override val input: Input
) : CreateRequest<Caller, Input>(),
    ListEndpointRequest.Create.Execute<Caller, Nothing, Input>

abstract class AuthorizeCreate<Caller> :
    CreateRequest<Caller, Nothing>(),
    ListEndpointRequest.Create.Authorize<Caller, Nothing>

//</editor-fold>

//-----------------------------------------------------------
// Update Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

sealed class UpdateRequest<Caller, Id, Model, out Input> :
    ListResourceRequest<Caller>(),
    ListEndpointRequest.Update<Caller, Nothing, Id, Model, Input>

abstract class ExecuteUpdate<Caller, Id, Model, Input> :
    UpdateRequest<Caller, Id, Model, Input>(),
    ListEndpointRequest.Update.Execute<Caller, Nothing, Id, Model, Input>

abstract class AuthorizeUpdate<Caller, Id, Model> :
    UpdateRequest<Caller, Id, Model, Nothing>(),
    ListEndpointRequest.Update.Authorize<Caller, Nothing, Id, Model>


//</editor-fold>

//-----------------------------------------------------------
// Create Or Update Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

sealed class CreateOrUpdate<Caller, Id, out Model, out Input> :
    ListResourceRequest<Caller>(),
    ListEndpointRequest.CreateOrUpdate<Caller, Nothing, Id, Model, Input> {

    abstract class ExecuteCreate<Caller, Id, Input> :
        CreateOrUpdate<Caller, Id, Nothing, Input>(),
        ListEndpointRequest.CreateOrUpdate.ExecuteCreate<Caller, Nothing, Id, Input>

    abstract class ExecuteUpdate<Caller, Id, Model, Input> :
        CreateOrUpdate<Caller, Id, Model, Input>(),
        ListEndpointRequest.CreateOrUpdate.ExecuteUpdate<Caller, Nothing, Id, Model, Input>

    abstract class Authorize<Caller, Id, Model> :
        CreateOrUpdate<Caller, Id, Model?, Nothing>(),
        ListEndpointRequest.CreateOrUpdate.Authorize<Caller, Nothing, Id, Model>


}

//</editor-fold>

//-----------------------------------------------------------
// Delete Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

abstract class DeleteRequest<Caller, Id, Model> :
    ListResourceRequest<Caller>(),
    ListEndpointRequest.Delete<Caller, Nothing, Id, Model>

//</editor-fold>
