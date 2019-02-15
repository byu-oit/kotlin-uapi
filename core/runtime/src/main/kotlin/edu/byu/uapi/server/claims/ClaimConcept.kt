package edu.byu.uapi.server.claims

import edu.byu.uapi.model.UAPIClaimRelationship
import edu.byu.uapi.server.resources.ResourceRequestContext
import kotlin.reflect.KClass

interface ClaimConcept<UserContext : Any, SubjectId : Any, Model : Any, Value : Any> {

    val valueType: KClass<Value>
    val name: String

    fun canUserEvaluateClaim(
        requestContext: ResourceRequestContext,
        user: UserContext,
        subjectId: SubjectId,
        subjectModel: Model
    ): Boolean

    fun getClaimEvaluator(relationship: UAPIClaimRelationship): ClaimEvaluator<UserContext, SubjectId, Model, Value>?

}

typealias ClaimEvaluator<User, SubjectId, Model, Value> = (
    requestContext: ResourceRequestContext,
    user: User,
    subjectId: SubjectId,
    subjectModel: Model,
    requestedValue: Value
) -> ClaimEvaluationResult

sealed class ClaimEvaluationResult {
    data class Success(val result: Boolean) : ClaimEvaluationResult()
    object NotAuthorized : ClaimEvaluationResult()
    data class Error(val code: Int, val messages: List<String>, val cause: Throwable? = null) :
        ClaimEvaluationResult() {
        constructor(
            code: Int,
            message: String,
            cause: Throwable? = null
        ) : this(
            code,
            listOf(message),
            cause
        )

        constructor(code: Int, vararg messages: String) : this(code, messages.toList())
    }
}

abstract class SimpleConcept<UserContext : Any, SubjectId : Any, Model : Any, Value : Any>
    : ClaimConcept<UserContext, SubjectId, Model, Value> {

    abstract val comparator: Comparator<Value>

    abstract fun getValue(
        requestContext: ResourceRequestContext,
        user: UserContext,
        subjectId: SubjectId,
        subjectModel: Model
    ): ClaimValueResult<Value>


    override fun getClaimEvaluator(relationship: UAPIClaimRelationship): ClaimEvaluator<UserContext, SubjectId, Model, Value>? {
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        return when (relationship) {
            UAPIClaimRelationship.GREATER_THAN          -> getEvaluator { it > 0 }
            UAPIClaimRelationship.GREATER_THAN_OR_EQUAL -> getEvaluator { it >= 0 }
            UAPIClaimRelationship.LESS_THAN             -> getEvaluator { it < 0 }
            UAPIClaimRelationship.LESS_THAN_OR_EQUAL    -> getEvaluator { it <= 0 }
            UAPIClaimRelationship.EQUAL                 -> getEvaluator { it == 0 }
            UAPIClaimRelationship.NOT_EQUAL             -> getEvaluator { it != 0 }
            else                                        -> null
        }
    }

    private fun getEvaluator(evaluateResult: (Int) -> Boolean): ClaimEvaluator<UserContext, SubjectId, Model, Value> =
        { requestContext, user, subjectId, model, value ->
            val current = getValue(requestContext, user, subjectId, model)
            when (current) {
                is ClaimValueResult.Error      -> ClaimEvaluationResult.Error(
                    current.code,
                    current.messages.joinToString()
                )
                ClaimValueResult.NotAuthorized -> ClaimEvaluationResult.NotAuthorized
                ClaimValueResult.None          -> ClaimEvaluationResult.Success(false)
                is ClaimValueResult.Value      -> {
                    val compareResult = comparator.compare(current.value, value)
                    ClaimEvaluationResult.Success(evaluateResult(compareResult))
                }
            }
        }
}

abstract class SimpleComparableConcept<UserContext : Any, SubjectId : Any, Model : Any, Value : Comparable<Value>>
    : SimpleConcept<UserContext, SubjectId, Model, Value>() {

    override val comparator: Comparator<Value> = java.util.Comparator.naturalOrder()
}


sealed class ClaimValueResult<V : Any> {
    data class Value<V : Any>(val value: V) : ClaimValueResult<V>()
    data class Error(
        val code: Int,
        val messages: List<String>,
        val cause: Throwable? = null
    ) : ClaimValueResult<Nothing>() {
        constructor(code: Int, message: String, cause: Throwable? = null) : this(code, listOf(message), cause)
        constructor(code: Int, vararg messages: String) : this(code, messages.toList())
    }

    object NotAuthorized : ClaimValueResult<Nothing>()
    object None : ClaimValueResult<Nothing>()
}
