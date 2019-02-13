package edu.byu.uapi.server.claims

import edu.byu.uapi.model.UAPIClaimRelationship
import edu.byu.uapi.server.inputs.create
import edu.byu.uapi.server.resources.ResourceRequestContext
import edu.byu.uapi.server.util.DarkMagic
import edu.byu.uapi.server.util.DarkMagicException
import edu.byu.uapi.spi.UAPITypeError
import kotlin.reflect.KClass

interface ClaimConcept<UserContext : Any, Model : Any, Value : Any> {

    val name: String
    val valueType: KClass<Value>
        get() = defaultGetValueType()

    fun canUserEvaluateClaim(
        requestContext: ResourceRequestContext,
        user: UserContext,
        model: Model
    ): Boolean

    fun getClaimEvaluator(relationship: UAPIClaimRelationship): ClaimEvaluator<UserContext, Model, Value>?

}

typealias ClaimEvaluator<User, Model, Value> = (
    requestContext: ResourceRequestContext,
    user: User,
    model: Model,
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

abstract class SimpleConcept<UserContext : Any, Model : Any, Value : Any> : ClaimConcept<UserContext, Model, Value> {

    abstract val comparator: Comparator<Value>

    abstract fun getValue(
        requestContext: ResourceRequestContext,
        user: UserContext,
        model: Model
    ): ClaimValueResult<Value>


    override fun getClaimEvaluator(relationship: UAPIClaimRelationship): ClaimEvaluator<UserContext, Model, Value>? {
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

    private fun getEvaluator(evaluateResult: (Int) -> Boolean): ClaimEvaluator<UserContext, Model, Value> =
        { requestContext, user, model, value ->
            val current = getValue(requestContext, user, model)
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

abstract class SimpleComparableConcept<UserContext : Any, Model : Any, Value : Comparable<Value>>
    : SimpleConcept<UserContext, Model, Value>() {

    override val comparator: Comparator<Value> = java.util.Comparator.naturalOrder()
}

@Throws(UAPITypeError::class)
internal fun <Value : Any>
    ClaimConcept<*, *, Value>.defaultGetValueType(): KClass<Value> {
    try {
        return DarkMagic.findSupertypeArgNamed(this::class, ClaimConcept::class, "Value")
    } catch (ex: DarkMagicException) {
        throw UAPITypeError.create(this::class, "Unable to get create input type", ex)
    }
}

sealed class ClaimValueResult<V : Any> {
    data class Value<V : Any>(val value: V) : ClaimValueResult<V>()
    data class Error(val code: Int, val messages: List<String>, val cause: Throwable? = null) : ClaimValueResult<Nothing>() {
        constructor(code: Int, message: String, cause: Throwable? = null) : this(code, listOf(message), cause)
        constructor(code: Int, vararg messages: String) : this(code, messages.toList())
    }

    object NotAuthorized : ClaimValueResult<Nothing>()
    object None : ClaimValueResult<Nothing>()
}
