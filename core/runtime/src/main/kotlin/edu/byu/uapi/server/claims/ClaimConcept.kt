package edu.byu.uapi.server.claims

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

    val comparator: Comparator<Value>

    fun canUserMakeClaims(
        requestContext: ResourceRequestContext,
        user: UserContext,
        model: Model
    ): Boolean

    fun getValue(
        requestContext: ResourceRequestContext,
        user: UserContext,
        model: Model
    ): ClaimValueResult<Value>

    val supportedRelationships: Set<UAPIClaimRelationship>
        get() = UAPIClaimRelationship.values().toSet()

    interface ComparableConcept<UserContext : Any, Model : Any, Value : Comparable<Value>>
        : ClaimConcept<UserContext, Model, Value> {

        override val comparator: Comparator<Value>
            get() = java.util.Comparator.naturalOrder()

    }
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
    data class Error(val code: Int, val messages: List<String>) : ClaimValueResult<Nothing>() {
        constructor(code: Int, message: String) : this(code, listOf(message))
        constructor(code: Int, vararg messages: String) : this(code, messages.toList())
    }

    object NotAuthorized : ClaimValueResult<Nothing>()
    object None : ClaimValueResult<Nothing>()
}

enum class UAPIClaimRelationship(
    val apiValue: String
) {
    GREATER_THAN("gt"),
    GREATER_THAN_OR_EQUAL("gt_or_eq"),
    LESS_THAN("lt"),
    LESS_THAN_OR_EQUAL("lt_or_eq"),
    EQUAL("eq"),
    NOT_EQUAL("not_eq")
}
