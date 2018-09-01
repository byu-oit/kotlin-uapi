package edu.byu.uapi.server.resources.singleton


interface SingletonResource<UserContext: Any, Model: Any> {
    fun loadModel(userContext: UserContext)
    fun canUserViewModel(userContext: UserContext, model: Model)
}

