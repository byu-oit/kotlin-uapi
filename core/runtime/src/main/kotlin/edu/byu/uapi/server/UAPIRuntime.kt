package edu.byu.uapi.server

import edu.byu.uapi.server.inputs.DefaultTypeDictionary
import edu.byu.uapi.server.resources.list.ListResource
import edu.byu.uapi.server.resources.list.ListResourceRuntime
import edu.byu.uapi.server.resources.Resource
import edu.byu.uapi.server.subresources.Subresource
import edu.byu.uapi.server.types.IdentifiedModel
import edu.byu.uapi.server.types.ModelHolder
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.scalars.ScalarType
import edu.byu.uapi.spi.validation.ValidationEngine
import edu.byu.uapi.validation.hibernate.HibernateValidationEngine
import java.util.*

class UAPIRuntime<UserContext : Any>(
    options: Options<UserContext>
) {

    constructor(userContextFactory: UserContextFactory<UserContext>) : this(Options(userContextFactory))
//    constructor(init: RuntimeInit<UserContext>.() -> Unit) : this(RuntimeInit<UserContext>().apply { init() }.build())

    companion object {
        inline operator fun <UserContext : Any> invoke(init: RuntimeInit<UserContext>.() -> Unit): UAPIRuntime<UserContext> {
            val ri = RuntimeInit<UserContext>()
            ri.init()
            return ri.build()
        }
    }

    val userContextFactory = options.userContextFactory
    val typeDictionary = options.typeDictionary
    val validationEngine = options.validationEngine

    private val resources: MutableMap<String, ListResourceRuntime<UserContext, *, *, *>> = mutableMapOf()

    fun <Id : Any, Model : Any> register(
        resource: ListResource<UserContext, Id, Model, *>,
        subresources: List<Subresource<UserContext, IdentifiedModel<Id, Model>, Model>> = emptyList()
    ) {
        val runtime = ListResourceRuntime(resource, typeDictionary, validationEngine, emptyList())
        resources[resource.pluralName] = runtime
        //TODO: Validate resource
    }

    internal fun <Model: Any, Parent: ModelHolder> register(mapping: ResourceMapping<UserContext, Model, Parent>) {
        val (resource, subs) = mapping

        if (resource !is ListResource<*, *, *, *>) {
            TODO("Singleton resources are not yet supported")
        }

        val runtime = ListResourceRuntime(resource, typeDictionary, validationEngine, emptyList())
//        resources[resource.pluralName] = runtime
        //TODO: Validate resource
    }

    fun resources(): Map<String, ListResourceRuntime<UserContext, *, *, *>> = Collections.unmodifiableMap(resources)

    data class Options<UserContext : Any>(
        val userContextFactory: UserContextFactory<UserContext>,
        val typeDictionary: TypeDictionary = DefaultTypeDictionary(),
        val validationEngine: ValidationEngine = HibernateValidationEngine
    ) {
        constructor(
            userContextFactory: UserContextFactoryFunc<UserContext>,
            typeDictionary: TypeDictionary = DefaultTypeDictionary(),
            validationEngine: ValidationEngine = HibernateValidationEngine
        ) : this(
            userContextFactory = UserContextFactory.from(userContextFactory),
            typeDictionary = typeDictionary,
            validationEngine = validationEngine
        )
    }
}

internal data class ResourceMapping<UserContext : Any, Model: Any, Parent: ModelHolder>(
    val resource: Resource<UserContext, Model, Parent>,
    val subresources: List<Subresource<UserContext, Parent, Model>> = emptyList()
)

class RuntimeInit<UserContext : Any> {

    lateinit var userContextFactory: UserContextFactory<UserContext>

//    fun userContextFactory(factoryFunc: UserContextFactoryFunc<UserContext>) {
//        userContextFactory = UserContextFactory.from(factoryFunc)
//    }

    var typeDictionary: TypeDictionary = DefaultTypeDictionary()
    var validationEngine: ValidationEngine = HibernateValidationEngine

    private val scalars: MutableList<ScalarType<*>> = mutableListOf()

    operator fun ScalarType<*>.unaryPlus() {
        scalars.add(this)
    }

    private val resources: MutableList<ResourceMapping<UserContext, *, *>> = mutableListOf()

    operator fun Resource<UserContext, *, *>.unaryPlus() {
        resources += ResourceMapping(this)
    }

    operator fun <Model : Any, SubresourceStyle: ModelHolder> Pair<Resource<UserContext, Model, SubresourceStyle>, List<Subresource<UserContext, SubresourceStyle, Model>>>.unaryPlus() {
        resources += ResourceMapping(this.first, this.second)
    }

    infix fun <A, B> A.with(that: B): Pair<A, B> = this to that

    fun build(): UAPIRuntime<UserContext> {
        scalars.forEach { typeDictionary.registerScalarType(it) }
        return UAPIRuntime(UAPIRuntime.Options(
            userContextFactory = userContextFactory,
            typeDictionary = typeDictionary,
            validationEngine = validationEngine
        )).also { r ->
            resources.forEach { r.register(it) }
        }
    }
}
