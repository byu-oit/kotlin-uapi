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
import kotlin.reflect.KClass

class ClaimsRuntime<UserContext : Any, SubjectId : Any, Model : Any>(
    private val loader: ClaimModelLoader<UserContext, SubjectId, Model>,
    typeDictionary: TypeDictionary,
    val idType: ScalarType<SubjectId>,
    conceptList: List<ClaimConcept<UserContext, SubjectId, Model, *, *>>
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
        LOG.info { "Claim results: $result" }
        return resultToResponse(result)
    }

    fun evaluate(
        requestContext: ResourceRequestContext,
        user: UserContext,
        request: MultiClaimRequest<SubjectId>
    ): UAPIResponse<*> {
        val map = request.claims.mapValues { evaluate(requestContext, user, it.value) }
        return MultiClaimResponse(map)
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
            ?: return EvalResult.Error(404, listOf("Subject does not exist"))

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
            ClaimEvaluationMode.ANY -> resultList.any { it }
        }
        return EvalResult.Success(result)
    }

    @Suppress("UNCHECKED_CAST")
    private fun matchConcept(assertion: ClaimAssertion): MatchedConcept<UserContext, SubjectId, Model, *>? {
        return (concepts[assertion.concept] as? ConceptRuntime<UserContext, SubjectId, Model, *, *>?)?.let {
            MatchedConcept(assertion, it)
        }
    }

    private fun String.sanitize(): String {
        return this.replace("""[^-a-zA-Z0-9_]""".toRegex(), "")
    }
}

class ConceptRuntime<UserContext : Any, SubjectId : Any, Model : Any, Value : Any, Qualifiers: Any>(
    val concept: ClaimConcept<UserContext, SubjectId, Model, Value, Qualifiers>,
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

    @Suppress("UNCHECKED_CAST")
    private fun parseQualifiers(assertion: ClaimAssertion): Qualifiers {
        val type = concept.qualifiersType
        if (type == Any::class) return Any() as Qualifiers
        return assertion.getQualifiers(type)
    }

    internal fun evaluate(
        requestContext: ResourceRequestContext,
        userContext: UserContext,
        id: SubjectId,
        model: Model,
        assertion: ClaimAssertion
    ): EvalResult {
        val qualifiers = parseQualifiers(assertion)
        val value = valueType.fromString(assertion.value)
        if (!concept.canUserEvaluateClaim(requestContext, userContext, id, model, qualifiers)) {
            return EvalResult.NotAuthorized
        }
        val e = evaluators.getOrElse(assertion.relationship) {
            return EvalResult.UnsupportedRelationship(concept.name, assertion.relationship)
        }
        return when (val result = e.invoke(requestContext, userContext, id, model, value, qualifiers)) {
            is ClaimEvaluationResult.Success    -> EvalResult.Success(result.result)
            ClaimEvaluationResult.NotAuthorized -> EvalResult.NotAuthorized
            is ClaimEvaluationResult.Error      -> EvalResult.Error(result.code, result.messages, result.cause)
        }
    }

    override fun introspect(context: IntrospectionContext): UAPIClaimModel {
        if (concept.qualifiersType != Any::class) {
            context.warn("Introspection of claim concept qualifiers is not yet supported.", "How to fix this: Clone Joseph so he can work on Campus Cards and refactoring the UAPI Runtime into something comprehensible at the same time.")
        }
        return model
    }
}

private data class MatchedConcept<UserContext : Any, SubjectId : Any, Model : Any, Value : Any>(
    val claim: ClaimAssertion,
    val concept: ConceptRuntime<UserContext, SubjectId, Model, Value, *>
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

data class MultiClaimRequest<SubjectId: Any>(
    val claims: Map<String, ClaimRequest<SubjectId>>
)

data class ClaimRequest<SubjectId : Any>(
    val subject: SubjectId,
    val mode: ClaimEvaluationMode,
    val claims: List<ClaimAssertion>
)

interface ClaimAssertion {
    val concept: String
    val relationship: UAPIClaimRelationship
    val value: String
    fun <Q: Any> getQualifiers(type: KClass<Q>): Q
}

enum class ClaimEvaluationMode(val apiValue: String) {
    ALL("ALL"),
    ANY("ANY")
}


