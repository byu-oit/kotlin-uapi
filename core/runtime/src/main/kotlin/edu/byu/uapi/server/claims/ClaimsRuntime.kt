package edu.byu.uapi.server.claims

import edu.byu.uapi.model.UAPIClaimModel
import edu.byu.uapi.model.UAPIClaimRelationship
import edu.byu.uapi.server.resources.ResourceRequestContext
import edu.byu.uapi.server.spi.requireScalarType
import edu.byu.uapi.server.types.*
import edu.byu.uapi.server.util.info
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.introspection.Introspectable
import edu.byu.uapi.spi.introspection.IntrospectionContext
import edu.byu.uapi.spi.scalars.ScalarType

class ClaimsRuntime<UserContext : Any, SubjectId : Any, Model : Any>(
    private val loader: ClaimModelLoader<UserContext, SubjectId, Model>,
    typeDictionary: TypeDictionary,
    val idType: ScalarType<SubjectId>,
    conceptList: List<ClaimConcept<UserContext, SubjectId, Model, *>>
) : Introspectable<Map<String, UAPIClaimModel>> {
    companion object {
        private val LOG = loggerFor<ClaimsRuntime<*, *, *>>()
    }

    val concepts = conceptList.associate {
        it.name to ConceptRuntime(it, typeDictionary)
    }

    fun evaluate(
        requestContext: ResourceRequestContext,
        user: UserContext,
        request: ClaimRequest<SubjectId>
    ): UAPIResponse<*> {
        LOG.info { "Evaluating claim request: $request" }
        val result = doEvaluate(requestContext, user, request)
        LOG.info { "Claim result: $result" }
        return resultToResponse(result)
    }

    override fun introspect(context: IntrospectionContext): Map<String, UAPIClaimModel> {
        return concepts.mapValues { it.value.introspect(context) }
    }

    private fun resultToResponse(result: EvalResult): UAPIResponse<*> {
        return when (result) {
            EvalResult.NotAuthorized              -> UAPINotAuthorizedError
            is EvalResult.InvalidConcept          -> UAPIBadRequestError("Invalid claim concept: '${result.concept.sanitize()}'")
            is EvalResult.UnsupportedRelationship -> UAPIBadRequestError("Claim '${result.concept.sanitize()}' does not support relationship '${result.relationship.apiValue}'.")
            is EvalResult.Success                 -> ClaimResponse(result.value)
            is EvalResult.Error                   -> {
                LOG.error("Error evaluating claims: code=${result.code}, messages=${result.messages}", result.cause)
                GenericUAPIErrorResponse(
                    result.code, "Unable to evaluate claims", result.messages
                )
            }
        }
    }

    private fun doEvaluate(
        requestContext: ResourceRequestContext,
        user: UserContext,
        request: ClaimRequest<SubjectId>
    ): EvalResult {
        // Match up claims to concepts and validate the incoming request
        val conceptMap = request.claims.map {
            matchConcept(it) ?: return EvalResult.InvalidConcept(it.concept)
        }.onEach {
            // Make sure
            val (claim, runtime) = it
            val rel = claim.relationship
            if (rel !in runtime.evaluators) {
                return EvalResult.UnsupportedRelationship(runtime.concept.name, rel)
            }
        }


        val model = loader.loadClaimModel(requestContext, user, request.subject)
            ?: return EvalResult.Success(false) //TODO: Is this really the right response for a bad subject ID?

        val authorized = loader.canUserMakeAnyClaims(requestContext, user, request.subject, model)
        if (!authorized) {
            return EvalResult.NotAuthorized
        }

        //Actually evaluate the claims
        val resultList: List<Boolean> = conceptMap.map {
            val r = it.evaluate(requestContext, user, request.subject, model)
            if (r !is EvalResult.Success) {
                return r
            }
            r.value
        }

        val result = when (request.mode) {
            ClaimEvaluationMode.ALL -> resultList.all { it }
            ClaimEvaluationMode.ONE -> resultList.count { it } == 1
            ClaimEvaluationMode.ANY -> resultList.any { it }
        }
        return EvalResult.Success(result)
    }

    @Suppress("UNCHECKED_CAST")
    private fun matchConcept(assertion: ClaimAssertion): MatchedConcept<UserContext, SubjectId, Model, *>? {
        return (concepts[assertion.concept] as? ConceptRuntime<UserContext, SubjectId, Model, *>?)?.let {
            MatchedConcept(assertion, it)
        }
    }

    private fun String.sanitize(): String {
        return this.replace("""[^-a-zA-Z0-9_]""".toRegex(), "")
    }
}

class ConceptRuntime<UserContext : Any, SubjectId : Any, Model : Any, Value : Any>(
    val concept: ClaimConcept<UserContext, SubjectId, Model, Value>,
    typeDictionary: TypeDictionary
) : Introspectable<UAPIClaimModel> {
    val valueType = typeDictionary.requireScalarType(concept.valueType)

    val evaluators = UAPIClaimRelationship.values().mapNotNull {
        when (val e = concept.getClaimEvaluator(it)) {
            null -> null
            else -> it to e
        }
    }.toMap()

    val model = UAPIClaimModel(
        type = valueType.valueType,
        constraints = valueType.constraints,
        relationships = evaluators.keys
    )

    internal fun evaluate(
        requestContext: ResourceRequestContext,
        userContext: UserContext,
        id: SubjectId,
        model: Model,
        assertion: ClaimAssertion
    ): EvalResult {
        val value = valueType.fromString(assertion.value)
        if (!concept.canUserEvaluateClaim(requestContext, userContext, id, model)) {
            return EvalResult.NotAuthorized
        }
        val e = evaluators.getOrElse(assertion.relationship) {
            return EvalResult.UnsupportedRelationship(concept.name, assertion.relationship)
        }
        return when (val result = e.invoke(requestContext, userContext, id, model, value)) {
            is ClaimEvaluationResult.Success    -> EvalResult.Success(result.result)
            ClaimEvaluationResult.NotAuthorized -> EvalResult.NotAuthorized
            is ClaimEvaluationResult.Error      -> EvalResult.Error(result.code, result.messages, result.cause)
        }
    }

    override fun introspect(context: IntrospectionContext): UAPIClaimModel = model
}

private data class MatchedConcept<UserContext : Any, SubjectId : Any, Model : Any, Value : Any>(
    val claim: ClaimAssertion,
    val concept: ConceptRuntime<UserContext, SubjectId, Model, Value>
) {
    fun evaluate(
        requestContext: ResourceRequestContext,
        userContext: UserContext,
        id: SubjectId,
        model: Model
    ) = concept.evaluate(
        requestContext, userContext, id, model, claim
    )
}

internal sealed class EvalResult {
    object NotAuthorized : EvalResult()
    data class InvalidConcept(val concept: String) : EvalResult()
    data class UnsupportedRelationship(val concept: String, val relationship: UAPIClaimRelationship) : EvalResult()
    data class Success(val value: Boolean) : EvalResult()
    data class Error(val code: Int, val messages: List<String>, val cause: Throwable? = null) : EvalResult()
}

interface ClaimModelLoader<UserContext : Any, SubjectId : Any, Model : Any> {
    fun loadClaimModel(
        request: ResourceRequestContext,
        userContext: UserContext,
        id: SubjectId
    ): Model?

    fun canUserMakeAnyClaims(
        requestContext: ResourceRequestContext,
        userContext: UserContext,
        id: SubjectId,
        model: Model
    ): Boolean
}

data class ClaimRequest<SubjectId : Any>(
    val subject: SubjectId,
    val mode: ClaimEvaluationMode,
    val claims: List<ClaimAssertion>
)

data class ClaimAssertion(
    val concept: String,
    val relationship: UAPIClaimRelationship,
    val value: String
    //TODO: Qualifiers
)

enum class ClaimEvaluationMode(val apiValue: String) {
    ALL("ALL"),
    ONE("ONE"),
    ANY("ANY")
}


