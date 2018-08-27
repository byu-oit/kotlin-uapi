package edu.byu.uapi.server


interface SingletonResource<UserContext: Any, Model: Any> {
    fun loadModel(userContext: UserContext)
    fun canUserViewModel(userContext: UserContext, model: Model)
}

