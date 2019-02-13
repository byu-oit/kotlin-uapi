package edu.byu.uapi.server.claims

import edu.byu.uapi.model.UAPIClaimRelationship
import edu.byu.uapi.server.resources.Resource
import edu.byu.uapi.server.resources.ResourceRequestContext
import edu.byu.uapi.server.response.ValuePropGetter
import java.lang.annotation.Inherited
import java.util.*
import kotlin.reflect.full.isSuperclassOf

fun <UserContext : Any, Model : Any> Resource.HasClaims<UserContext, Model, *>.claimConcepts(
    init: ConceptListInit<UserContext, Model>.() -> Unit
): List<ClaimConcept<UserContext, Model, *>> {
    val i = ConceptListInit<UserContext, Model>()
    i.init()
    return i.concepts
}

@InConceptDsl
class ConceptListInit<UserContext : Any, Model : Any> {

    @PublishedApi
    internal val concepts: MutableList<ClaimConcept<UserContext, Model, *>> = mutableListOf()

    inline fun <reified Value : Any> concept(
        name: String,
        init: ConceptInit<UserContext, Model, Value>.() -> Unit
    ) {
        @Suppress("CAST_NEVER_SUCCEEDS")
        val v: Value = null as Value
        val comp: Comparator<Value>? = if (Comparable::class.isSuperclassOf(Value::class)) {
            TODO()
        } else {
            null
        }
        val concept = ConceptInit<UserContext, Model, Value>(name)
    }
}

@DslMarker
@Inherited
annotation class InConceptDsl

@InConceptDsl
class ConceptInit<UserContext : Any, Model : Any, Value : Any>(
    val name: String,
    var comparator: Comparator<Value>? = null
) {

    private lateinit var getter: ValuePropGetter<Model, Value>

    fun getValue(fn: ValuePropGetter<Model, Value>) {
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

}

typealias ConceptAuthZ<UserContext, Model> = (requestContext: ResourceRequestContext, user: UserContext, model: Model) -> Boolean
