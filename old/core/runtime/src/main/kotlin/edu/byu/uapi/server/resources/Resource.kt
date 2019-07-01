package edu.byu.uapi.server.resources

import edu.byu.uapi.server.claims.ClaimConcept
import edu.byu.uapi.server.spi.requireScalarType
import edu.byu.uapi.server.types.ModelHolder
import edu.byu.uapi.server.util.extrapolateGenericType
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.scalars.ScalarType
import edu.byu.uapi.utility.takeIfType
import kotlin.reflect.KClass

interface Resource<UserContext : Any, Model : Any, ModelStyle : ModelHolder> {

    val claims: Resource.HasClaims<UserContext, *, Model, ModelStyle>?
        get() = this.takeIfType()

    interface HasClaims<
        UserContext : Any,
        SubjectId : Any,
        Model : Any,
        ModelStyle : ModelHolder
        > : Resource<UserContext, Model, ModelStyle> {

        val claimSubjectIdType: KClass<SubjectId>
            get() = extrapolateGenericType("SubjectId", HasClaims<*, *, *, *>::claimSubjectIdType)

        fun getClaimIdScalar(typeDictionary: TypeDictionary): ScalarType<SubjectId> {
            return typeDictionary.requireScalarType(claimSubjectIdType)
        }

        fun canUserMakeAnyClaims(
            requestContext: ResourceRequestContext,
            user: UserContext,
            subject: SubjectId,
            subjectModel: Model
        ): Boolean

        val claimConcepts: List<ClaimConcept<UserContext, SubjectId, Model, *, *>>
    }

}

