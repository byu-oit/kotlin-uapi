package edu.byu.uapi.server.style.receiverfn.requests.subresource.list

import edu.byu.uapi.server.style.receiverfn.requests.ListEndpointRequest
import edu.byu.uapi.server.style.receiverfn.requests.subresource.SubresourceRequest

sealed class ListSubresourceRequest<Caller, Parent> :
    ListEndpointRequest<Caller, Parent>,
    SubresourceRequest<Caller, Parent>

//-----------------------------------------------------------
// Create Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

abstract class Load<Caller, Parent, Id> :
    ListSubresourceRequest<Caller, Parent>(),
    ListEndpointRequest.Load<Caller, Parent, Id>

abstract class CanView<Caller, Parent, Id, Model> :
    ListSubresourceRequest<Caller, Parent>(),
    ListEndpointRequest.CanView<Caller, Parent, Id, Model>

abstract class ListRequest<Caller, Parent, Params> :
    ListSubresourceRequest<Caller, Parent>(),
    ListEndpointRequest.List<Caller, Parent, Params>

//</editor-fold>

//-----------------------------------------------------------
// Create Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

sealed class CreateRequest<Caller, Parent, out Input> :
    ListSubresourceRequest<Caller, Parent>(),
    ListEndpointRequest.Create<Caller, Parent, Input>

abstract class ExecuteCreate<Caller, Parent, Input> :
    CreateRequest<Caller, Parent, Input>(),
    ListEndpointRequest.Create.Execute<Caller, Parent, Input>

abstract class AuthorizeCreate<Caller, Parent> :
    CreateRequest<Caller, Parent, Nothing>(),
    ListEndpointRequest.Create.Authorize<Caller, Parent>

//</editor-fold>

//-----------------------------------------------------------
// Update Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

sealed class UpdateRequest<Caller, Parent, Id, Model, out Input> :
    ListSubresourceRequest<Caller, Parent>(),
    ListEndpointRequest.Update<Caller, Parent, Id, Model, Input>

abstract class ExecuteUpdate<Caller, Parent, Id, Model, Input> :
    UpdateRequest<Caller, Parent, Id, Model, Input>(),
    ListEndpointRequest.Update.Execute<Caller, Parent, Id, Model, Input>

abstract class AuthorizeUpdate<Caller, Parent, Id, Model> :
    UpdateRequest<Caller, Parent, Id, Model, Nothing>(),
    ListEndpointRequest.Update.Authorize<Caller, Parent, Id, Model>

//</editor-fold>

//-----------------------------------------------------------
// Create or Update Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

sealed class CreateOrUpdate<Caller, Parent, Id, out Model, out Input> :
    ListSubresourceRequest<Caller, Parent>(),
    ListEndpointRequest.CreateOrUpdate<Caller, Parent, Id, Model, Input> {

    abstract class ExecuteCreate<Caller, Parent, Id, Input> :
        CreateOrUpdate<Caller, Parent, Id, Nothing, Input>(),
        ListEndpointRequest.CreateOrUpdate.ExecuteCreate<Caller, Parent, Id, Input>

    abstract class ExecuteUpdate<Caller, Parent, Id, Model, Input> :
        CreateOrUpdate<Caller, Parent, Id, Model, Input>(),
        ListEndpointRequest.CreateOrUpdate.ExecuteUpdate<Caller, Parent, Id, Model, Input>

    abstract class Authorize<Caller, Parent, Id, Model> :
        CreateOrUpdate<Caller, Parent, Id, Model?, Nothing>(),
        ListEndpointRequest.CreateOrUpdate.Authorize<Caller, Parent, Id, Model>

}

//</editor-fold>

//-----------------------------------------------------------
// Delete Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

abstract class DeleteRequest<Caller, Parent, Id, Model> :
    ListSubresourceRequest<Caller, Parent>(),
    ListEndpointRequest.Delete<Caller, Parent, Id, Model>

//</editor-fold>
