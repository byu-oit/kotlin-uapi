package edu.byu.uapi.server.claims

import edu.byu.uapi.model.UAPIClaimRelationship
import edu.byu.uapi.server.resources.Resource
import edu.byu.uapi.server.resources.ResourceRequestContext
import edu.byu.uapi.server.response.ValuePropGetter
import edu.byu.uapi.server.util.DarkerMagic
import edu.byu.uapi.server.util.toSnakeCase
import java.lang.annotation.Inherited
import java.util.*
import kotlin.Comparator
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

fun <UserContext : Any, Id : Any, Model : Any> Resource.HasClaims<UserContext, Id, Model, *>.claimConcepts(
    init: ConceptListInit<UserContext, Id, Model>.() -> Unit
): List<ClaimConcept<UserContext, Id, Model, *>> {
    val i = ConceptListInit<UserContext, Id, Model>()
    i.init()
    return i.concepts
}

@InConceptDsl
class ConceptListInit<UserContext : Any, Id : Any, Model : Any> {

    @PublishedApi
    internal val concepts: MutableList<ClaimConcept<UserContext, Id, Model, *>> = mutableListOf()

    inline fun <reified Value : Any> concept(
        name: String,
        init: ConceptInit<UserContext, Id, Model, Value>.() -> Unit
    ) {
        val type = Value::class
        val comp = DarkerMagic.maybeNaturalComparatorFor(type)
        val concept = ConceptInit<UserContext, Id, Model, Value>(type, name, comp)
        concept.init()
        concepts += concept.build()
    }

    inline fun <reified Value : Any> concept(
        prop: KProperty1<Model, Value>,
        init: ConceptInit<UserContext, Id, Model, Value>.() -> Unit
    ) {
        val type = Value::class
        val comp = DarkerMagic.maybeNaturalComparatorFor(type)
        val concept = ConceptInit<UserContext, Id, Model, Value>(type, prop.name.toSnakeCase(), comp)
        concept.getValue {
//            try {
                ClaimValueResult.Value(prop.get(it))
//            } catch (err: Exception) {
//            }
        }
        concept.init()
        concepts += concept.build()
    }
}

@DslMarker
@Inherited
annotation class InConceptDsl

@InConceptDsl
class ConceptInit<UserContext : Any, Id : Any, Model : Any, Value : Any>(
    val type: KClass<Value>,
    val name: String,
    var comparator: Comparator<Value>? = null
) {

    private lateinit var getter: ValuePropGetter<Model, ClaimValueResult<Value>>

    fun getValue(fn: ValuePropGetter<Model, ClaimValueResult<Value>>) {
        getter = fn
    }

    protected lateinit var userAuth: ConceptAuthZ<UserContext, Model>

    fun canUserMakeClaim(authFn: ConceptAuthZ<UserContext, Model>) {
        this.userAuth = authFn
    }

    fun <R : Comparable<R>> compareUsing(fn: (Value) -> R?) {
        comparator = Comparator.comparing<Value, R>(fn)
    }

    var supports: Set<UAPIClaimRelationship> = EnumSet.allOf(UAPIClaimRelationship::class.java)

    @PublishedApi
    internal fun build(): ClaimConcept<UserContext, Id, Model, Value> {
        return ClaimConceptImpl(
            name,
            type,
            comparator
                ?: throw RuntimeException("You must pass a function to 'compareUsing' for concept $name, or make $type implement the Comparable interface."),
            this.supports,
            userAuth,
            getter
        )
    }
}

internal class ClaimConceptImpl<UserContext : Any, Id : Any, Model : Any, Value : Any>(
    override val name: String,
    override val valueType: KClass<Value>,
    override val comparator: Comparator<Value>,
    private val supportedRelationships: Set<UAPIClaimRelationship>,
    private val authz: ConceptAuthZ<UserContext, Model>,
    private val getter: ValuePropGetter<Model, ClaimValueResult<Value>>
) : SimpleConcept<UserContext, Id, Model, Value>() {

    override fun getClaimEvaluator(relationship: UAPIClaimRelationship): ClaimEvaluator<UserContext, Id, Model, Value>? {
        if (relationship !in supportedRelationships) {
            return null
        }
        return super.getClaimEvaluator(relationship)
    }

    override fun canUserEvaluateClaim(
        requestContext: ResourceRequestContext,
        user: UserContext,
        subjectId: Id,
        subjectModel: Model
    ): Boolean {
        return authz.invoke(requestContext, user, subjectModel)
    }

    override fun getValue(
        requestContext: ResourceRequestContext,
        user: UserContext,
        subjectId: Id,
        subjectModel: Model
    ): ClaimValueResult<Value> {
        return getter.invoke(subjectModel)
    }
}

typealias ConceptAuthZ<UserContext, Model> = (requestContext: ResourceRequestContext, user: UserContext, model: Model) -> Boolean
