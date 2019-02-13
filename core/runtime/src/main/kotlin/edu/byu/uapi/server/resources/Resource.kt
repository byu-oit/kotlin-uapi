package edu.byu.uapi.server.resources

import edu.byu.uapi.server.claims.ClaimConcept
import edu.byu.uapi.server.types.ModelHolder
import edu.byu.uapi.utility.takeIfType

interface Resource<UserContext : Any, Model : Any, ModelStyle : ModelHolder> {

    val claims: Resource.HasClaims<UserContext, Model, ModelStyle>?
        get() = this.takeIfType()

    interface HasClaims<
        UserContext : Any,
        Model : Any,
        ModelStyle : ModelHolder
        > : Resource<UserContext, Model, ModelStyle> {

        fun canUserMakeAnyClaims(user: UserContext, model: Model): Boolean

        val claimConcepts: List<ClaimConcept<UserContext, Model, *>>
    }

}
