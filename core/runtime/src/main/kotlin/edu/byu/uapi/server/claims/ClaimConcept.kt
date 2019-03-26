package edu.byu.uapi.server.claims

import edu.byu.uapi.model.UAPIClaimRelationship
import edu.byu.uapi.server.resources.ResourceRequestContext
import kotlin.reflect.KClass

interface ClaimConcept<UserContext : Any, SubjectId : Any, Model : Any, Value : Any, Qualifiers: Any> {

    val valueType: KClass<Value>
    val name: String
    val qualifiersType: KClass<Qualifiers>

    fun canUserEvaluateClaim(
        requestContext: ResourceRequestContext,
        user: UserContext,
        subjectId: SubjectId,
        subjectModel: Model,
        qualifiers: Qualifiers
    ): Boolean

    fun getClaimEvaluator(relationship: UAPIClaimRelationship): ClaimEvaluator<UserContext, SubjectId, Model, Value, Qualifiers>?

}

typealias ClaimEvaluator<User, SubjectId, Model, Value, Qualifiers> = (
    requestContext: ResourceRequestContext,
    user: User,
    subjectId: SubjectId,
    subjectModel: Model,
    requestedValue: Value,
    qualifiers: Qualifiers
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

typealias QualifiedComparator<Value, Qualifiers> = (left: Value, right: Value, qualifiers: Qualifiers) -> Int

abstract class SimpleConcept<UserContext : Any, SubjectId : Any, Model : Any, Value : Any, Qualifiers: Any>
    : ClaimConcept<UserContext, SubjectId, Model, Value, Qualifiers> {

    abstract val comparator: QualifiedComparator<Value, Qualifiers>

    abstract fun getValue(
        requestContext: ResourceRequestContext,
        user: UserContext,
        subjectId: SubjectId,
        subjectModel: Model,
        qualifiers: Qualifiers
    ): ClaimValueResult<Value>


    override fun getClaimEvaluator(relationship: UAPIClaimRelationship): ClaimEvaluator<UserContext, SubjectId, Model, Value, Qualifiers>? {
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

    private fun getEvaluator(evaluateResult: (Int) -> Boolean): ClaimEvaluator<UserContext, SubjectId, Model, Value, Qualifiers> =
        { requestContext, user, subjectId, model, value, qualifiers ->
            val current = getValue(requestContext, user, subjectId, model, qualifiers)
            when (current) {
                is ClaimValueResult.Error      -> ClaimEvaluationResult.Error(
                    current.code,
                    current.messages.joinToString()
                )
                ClaimValueResult.NotAuthorized -> ClaimEvaluationResult.NotAuthorized
                ClaimValueResult.None          -> ClaimEvaluationResult.Success(false)
                is ClaimValueResult.Value      -> {
                    val compareResult = comparator.invoke(current.value, value, qualifiers)
                    ClaimEvaluationResult.Success(evaluateResult(compareResult))
                }
            }
        }
}

abstract class SimpleComparableConcept<UserContext : Any, SubjectId : Any, Model : Any, Value : Comparable<Value>, Qualifiers: Any>
    : SimpleConcept<UserContext, SubjectId, Model, Value, Qualifiers>() {

    override val comparator: QualifiedComparator<Value, Qualifiers> = java.util.Comparator.naturalOrder<Value>().asQualifiedComparator()
}

fun <C: Any, Q: Any> Comparator<C>.asQualifiedComparator(): QualifiedComparator<C, Q> = {l, r, _ -> this.compare(l, r)}

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
