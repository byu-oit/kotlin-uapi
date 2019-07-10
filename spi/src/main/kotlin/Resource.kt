package edu.byu.uapi.server.spi

interface ResourceRequest : UAPIRequest {
    interface Load<Caller> :
        ResourceRequest,
        UAPIRequest.WithCaller<Caller>

    interface AuthorizeLoad<Caller, Model> :
        ResourceRequest,
        UAPIRequest.WithCaller<Caller>,
        UAPIRequest.WithModel<Model>

    interface Create<Caller, Input>:
        ResourceRequest,
        UAPIRequest.WithCaller<Caller>,
        UAPIRequest.WithInput<Input>

    interface Update<Caller, Input>:
        ResourceRequest,
        UAPIRequest.WithCaller<Caller>,
        UAPIRequest.WithInput<Input>
}

sealed class SingletonResourceRequest {
    data class Load<Caller>(
        override val caller: Caller
    ) : SingletonResourceRequest(), UAPIRequest.WithCaller<Caller>

    data class AuthorizeLoad<Caller, Model>(
        override val caller: Caller,
        override val model: Model
    ) : SingletonResourceRequest(),
        UAPIRequest.WithCaller<Caller>,
        UAPIRequest.WithModel<Model>
}

