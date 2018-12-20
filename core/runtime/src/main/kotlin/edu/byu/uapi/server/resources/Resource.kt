package edu.byu.uapi.server.resources

import edu.byu.uapi.server.types.ModelHolder

interface Resource<UserContext: Any, Model: Any, ModelStyle: ModelHolder> {
    val pretendUnauthorizedDoesntExist: Boolean
        get() = false
}
