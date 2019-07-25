package edu.byu.uapi.server.style.receiverfn

import edu.byu.uapi.server.style.receiverfn.requests.ListEndpointRequest
import edu.byu.uapi.server.style.receiverfn.requests.ListParams
import edu.byu.uapi.server.style.receiverfn.requests.ListRequest
import edu.byu.uapi.server.style.receiverfn.requests.ReadRequest
import edu.byu.uapi.server.style.receiverfn.requests.SingletonEndpointRequest

interface Endpoint<
    Caller,
    Parent,
    Model,
    LoadReq : ReadRequest.Load<Caller, Parent>,
    CanViewReq : ReadRequest.CanView<Caller, Parent, Model>
    > {

    suspend fun LoadReq.loadModel(): Model?
    suspend fun CanViewReq.canUserView(): Boolean

}

interface Resource<
    Caller,
    Model,
    LoadReq : ReadRequest.Load<Caller, Nothing>,
    CanViewReq : ReadRequest.CanView<Caller, Nothing, Model>
    > : Endpoint<Caller, Nothing, Model, LoadReq, CanViewReq>

interface Subresource<
    Caller,
    Parent,
    Model,
    LoadReq : ReadRequest.Load<Caller, Parent>,
    CanViewReq : ReadRequest.CanView<Caller, Parent, Model>
    > : Endpoint<Caller, Parent, Model, LoadReq, CanViewReq>

interface ListEndpoint<
    Caller,
    Parent,
    Id,
    Model,
    Params,
    LoadReq : ListEndpointRequest.Load<Caller, Parent, Id>,
    CanViewReq : ListEndpointRequest.CanView<Caller, Parent, Id, Model>,
    ListReq : ListEndpointRequest.List<Caller, Parent, Params>
    > : Endpoint<Caller, Parent, Model, LoadReq, CanViewReq>,
        ListFeature<Caller, Parent, Model, Params, ListReq> {

//    fun idFromModel(model: Model): Id
    val Model.uapiId: Id

}

interface ListFeature<Caller, Parent, Model, Params, Req : ListRequest<Caller, Parent, Params>> {

    suspend fun Req.list(): List<Model>

    interface Sort<
        Caller,
        Parent,
        Model,
        Field : Enum<Field>,
        Params : ListParams.WithSort<Field>,
        Req : ListRequest<Caller, Parent, Params>> :
        ListFeature<Caller, Parent, Model, Params, Req> {

    }

    interface Search<
        Caller,
        Parent,
        Model,
        Context : Enum<Context>,
        Params : ListParams.WithSearch<Context>,
        Req : ListRequest<Caller, Parent, Params>> :
        ListFeature<Caller, Parent, Model, Params, Req> {

    }

    interface Filter<
        Caller,
        Parent,
        Model,
        Filters,
        Params : ListParams.WithFilter<Filters>,
        Req : ListRequest<Caller, Parent, Params>> :
        ListFeature<Caller, Parent, Model, Params, Req> {

    }

    interface Subset<
        Caller,
        Parent,
        Model,
        Params : ListParams.WithSubset,
        Req : ListRequest<Caller, Parent, Params>> :
        ListFeature<Caller, Parent, Model, Params, Req> {

        override suspend fun Req.list(): ListWithTotal<Model>

    }

}

class ListWithTotal<T>(
    val totalCount: Int,
    val values: List<T>
): List<T> by values

interface SingletonEndpoint<
    Caller,
    Parent,
    Model,
    LoadReq : SingletonEndpointRequest.Load<Caller, Parent>,
    CanViewReq : SingletonEndpointRequest.CanView<Caller, Parent, Model>
    > : Endpoint<Caller, Parent, Model, LoadReq, CanViewReq> {

}
