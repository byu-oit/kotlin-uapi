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
): List<ClaimConcept<UserContext, Id, Model, *, *>> {
    val i = ConceptListInit<UserContext, Id, Model>()
    i.init()
    return i.finish()
}

@InConceptDsl
class ConceptListInit<UserContext : Any, Id : Any, Model : Any> {

    private val concepts: MutableList<ClaimConcept<UserContext, Id, Model, *, *>> = mutableListOf()
    private val startedContinuations: MutableMap<ConceptInitContinuation<UserContext, Id, Model, *>, Throwable> = mutableMapOf()

    internal fun finish(): List<ClaimConcept<UserContext, Id, Model, *, *>> {
        if (startedContinuations.isNotEmpty()) {
            val (continuation, at) = startedContinuations.entries.first()
            throw IllegalStateException("Started but did not finish initializing concept '${continuation.name}'; the full stack trace of where the initialization started follows as the 'cause'", at)
        }
        return concepts
    }

    inline fun <reified Value : Any> concept(
        name: String,
        init: ConceptInit<UserContext, Id, Model, Value>.() -> Unit
    ) {
        val concept = constructConceptInit(Value::class, name)
        concept.init()
        finishConceptInit(concept)
    }

    inline fun <reified Value : Any> concept(
        prop: KProperty1<Model, Value>,
        init: ConceptInit<UserContext, Id, Model, Value>.() -> Unit
    ) {
        val concept = constructConceptInit(Value::class, prop.name.toSnakeCase())
        concept.getValue { ClaimValueResult.Value(prop.get(it)) }
        concept.init()
        finishConceptInit(concept)
    }

    @PublishedApi
    internal fun finishConceptInit(init: ConceptInit<UserContext, Id, Model, *>) {
        concepts += init.build()
    }

    @PublishedApi
    internal fun <Value : Any> constructConceptInit(
        type: KClass<Value>,
        name: String
    ): ConceptInit<UserContext, Id, Model, Value> {
        return ConceptInit(
            type,
            name,
            DarkerMagic.maybeNaturalComparatorFor(type)
        )
    }

    inline fun <reified Value : Any> concept(name: String): ConceptInitContinuation<UserContext, Id, Model, Value> {
        return startContinuation(Value::class, name, null, Throwable())
    }

    inline fun <reified Value : Any> concept(prop: KProperty1<Model, Value>): ConceptInitContinuation<UserContext, Id, Model, Value> {
        return startContinuation(Value::class, prop.name.toSnakeCase(), prop, Throwable())
    }

    @PublishedApi
    internal fun <Value: Any> startContinuation(
        valueType: KClass<Value>,
        name: String,
        backingProp: KProperty1<Model, Value>?,
        startedAt: Throwable
    ): ConceptInitContinuation<UserContext, Id, Model, Value> {
        return ConceptInitContinuation(valueType, name, backingProp, this::finishContinuation)
            .also { startedContinuations += it to startedAt }
    }

    @PublishedApi
    internal fun finishContinuation(
        continuation: ConceptInitContinuation<UserContext, Id, Model, *>,
        concept: ClaimConcept<UserContext, Id, Model, *, *>
    ) {
        if (startedContinuations.remove(continuation) == null) {
            throw IllegalStateException("Invalid call to finishContinuation(): No corresponding call was made to startContinuation()")
        }
        concepts += concept
    }
}

class ConceptInitContinuation<UserContext : Any, Id : Any, Model : Any, Value : Any>(
//    private val parent: ConceptListInit<UserContext, Id, Model>,
    internal val valueType: KClass<Value>,
    internal val name: String,
    private val backingProperty: KProperty1<Model, Value>?,
    private val finishContinuation: (self: ConceptInitContinuation<UserContext, Id, Model, Value>, concept: ClaimConcept<UserContext, Id, Model, Value, *>) -> Unit
) {

    inline fun <reified Qualifiers : Any> qualifiedBy(
        init: QualifiedConceptInit<UserContext, Id, Model, Value, Qualifiers>.() -> Unit
    ) {
        val concept = constructQualifiedInit(Qualifiers::class)
        concept.init()
        finishConceptInit(concept)
    }

    @PublishedApi
    internal fun <Qualifiers : Any> constructQualifiedInit(qualifierType: KClass<Qualifiers>): QualifiedConceptInit<UserContext, Id, Model, Value, Qualifiers> {
        return QualifiedConceptInit<UserContext, Id, Model, Value, Qualifiers>(
            valueType,
            name,
            qualifierType,
            DarkerMagic.maybeNaturalComparatorFor(valueType)?.asQualifiedComparator()
        ).also {
            if (backingProperty != null) {
                it.getValue { model, _ -> ClaimValueResult.Value(backingProperty.get(model)) }
            }
        }
    }

    @PublishedApi
    internal fun finishConceptInit(init: QualifiedConceptInit<UserContext, Id, Model, Value, *>) {
        finishContinuation(this, init.build())
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

    private lateinit var getter: QualifiedConceptGetter<Model, Any, Value>

    fun getValue(fn: ValuePropGetter<Model, ClaimValueResult<Value>>) {
        getter = { m, _ -> fn(m) }
    }

    protected lateinit var userAuth: QualifiedConceptAuthZ<UserContext, Model, Any>

    fun canUserMakeClaim(authFn: ConceptAuthZ<UserContext, Model>) {
        this.userAuth = { ctx, u, m, _ -> authFn(ctx, u, m) }
    }

    fun <R : Comparable<R>> compareUsing(fn: (Value) -> R?) {
        comparator = Comparator.comparing<Value, R>(fn)
    }

    var supports: Set<UAPIClaimRelationship> = EnumSet.allOf(UAPIClaimRelationship::class.java)

    @PublishedApi
    internal fun build(): ClaimConcept<UserContext, Id, Model, Value, Any> {
        return ClaimConceptImpl(
            name,
            type,
            Any::class,
            comparator?.asQualifiedComparator()
                ?: throw RuntimeException("You must pass a function to 'compareUsing' for concept $name, or make $type implement the Comparable interface."),
            this.supports,
            userAuth,
            getter
        )
    }
}

@InConceptDsl
class QualifiedConceptInit<UserContext : Any, Id : Any, Model : Any, Value : Any, Qualifiers : Any>(
    val type: KClass<Value>,
    val name: String,
    val qualifiersType: KClass<Qualifiers>,
    var comparator: QualifiedComparator<Value, Qualifiers>? = null
) {

    private lateinit var getter: QualifiedConceptGetter<Model, Qualifiers, Value>

    fun getValue(fn: QualifiedConceptGetter<Model, Qualifiers, Value>) {
        getter = fn
    }

    protected lateinit var userAuth: QualifiedConceptAuthZ<UserContext, Model, Qualifiers>

    fun canUserMakeClaim(authFn: QualifiedConceptAuthZ<UserContext, Model, Qualifiers>) {
        this.userAuth = authFn
    }

    fun <R : Comparable<R>> compareUsing(fn: (Value, Qualifiers) -> R?) {
        val comp = naturalOrder<R>()
        comparator = {o1, o2, q ->
            val r1 = fn(o1, q)
            val r2 = fn(o2, q)
            comp.compare(r1, r2)
        }
    }

    var supports: Set<UAPIClaimRelationship> = EnumSet.allOf(UAPIClaimRelationship::class.java)

    @PublishedApi
    internal fun build(): ClaimConcept<UserContext, Id, Model, Value, Qualifiers> {
        return ClaimConceptImpl(
            name,
            type,
            qualifiersType,
            comparator
                ?: throw RuntimeException("You must pass a function to 'compareUsing' for concept $name, or make $type implement the Comparable interface."),
            this.supports,
            userAuth,
            getter
        )
    }
}

//typealias ConceptValueMapper<Value, Qualifiers, Mapped> = (Value, Qualifiers) -> Mapped?

internal class ClaimConceptImpl<UserContext : Any, Id : Any, Model : Any, Value : Any, Qualifiers : Any>(
    override val name: String,
    override val valueType: KClass<Value>,
    override val qualifiersType: KClass<Qualifiers>,
    override val comparator: QualifiedComparator<Value, Qualifiers>,
    private val supportedRelationships: Set<UAPIClaimRelationship>,
    private val authz: QualifiedConceptAuthZ<UserContext, Model, Qualifiers>,
    private val getter: QualifiedConceptGetter<Model, Qualifiers, Value>
) : SimpleConcept<UserContext, Id, Model, Value, Qualifiers>() {

    override fun getClaimEvaluator(relationship: UAPIClaimRelationship): ClaimEvaluator<UserContext, Id, Model, Value, Qualifiers>? {
        if (relationship !in supportedRelationships) {
            return null
        }
        return super.getClaimEvaluator(relationship)
    }

    override fun canUserEvaluateClaim(
        requestContext: ResourceRequestContext,
        user: UserContext,
        subjectId: Id,
        subjectModel: Model,
        qualifiers: Qualifiers
    ): Boolean {
        return authz.invoke(requestContext, user, subjectModel, qualifiers)
    }

    override fun getValue(
        requestContext: ResourceRequestContext,
        user: UserContext,
        subjectId: Id,
        subjectModel: Model,
        qualifiers: Qualifiers
    ): ClaimValueResult<Value> {
        return getter.invoke(subjectModel, qualifiers)
    }
}

typealias QualifiedConceptGetter<Model, Qualifiers, Value> = (model: Model, qualifiers: Qualifiers) -> ClaimValueResult<Value>
typealias ConceptAuthZ<UserContext, Model> = (requestContext: ResourceRequestContext, user: UserContext, model: Model) -> Boolean
typealias QualifiedConceptAuthZ<UserContext, Model, Qualifiers> = (requestContext: ResourceRequestContext, user: UserContext, model: Model, qualifiers: Qualifiers) -> Boolean
