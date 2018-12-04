package edu.byu.uapi.server

import edu.byu.uapi.server.inputs.DefaultTypeDictionary
import edu.byu.uapi.server.resources.list.ListResource
import edu.byu.uapi.server.resources.list.ListResourceRuntime
import edu.byu.uapi.server.resources.list.ResourceRuntime
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

    private val resources: MutableList<ResourceRuntime<UserContext, *>> = mutableListOf()

    fun <Id : Any, Model : Any> register(
        resource: ListResource<UserContext, Id, Model, *>,
        subresources: List<Subresource<UserContext, IdentifiedModel<Id, Model>, *>> = emptyList()
    ) {
        register(ResourceMapping.List(resource, subresources))
    }

    internal fun <Model : Any, Parent : ModelHolder> register(mapping: ResourceMapping<UserContext, Model, Parent>) {
        val runtime = mapping.toRuntime(typeDictionary, validationEngine)
        resources += runtime
        //TODO: Validate resource
    }

    fun resources(): List<ResourceRuntime<UserContext, *>> = Collections.unmodifiableList(resources)

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

internal sealed class ResourceMapping<UserContext : Any, Model : Any, Parent : ModelHolder> {

    abstract fun toRuntime(
        typeDictionary: TypeDictionary,
        validationEngine: ValidationEngine
    ): ResourceRuntime<UserContext, Parent>

    data class List<UserContext : Any, Id : Any, Model : Any>(
        val resource: ListResource<UserContext, Id, Model, *>,
        val subresources: kotlin.collections.List<Subresource<UserContext, IdentifiedModel<Id, Model>, *>> = emptyList()
    ) : ResourceMapping<UserContext, Model, IdentifiedModel<Id, Model>>() {
        override fun toRuntime(
            typeDictionary: TypeDictionary,
            validationEngine: ValidationEngine
        ): ResourceRuntime<UserContext, IdentifiedModel<Id, Model>> {
            return ListResourceRuntime(
                this.resource,
                typeDictionary,
                validationEngine,
                subresources
            )
        }
    }

}

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

    operator fun <Id : Any, Model : Any> ListResource<UserContext, Id, Model, *>.unaryPlus() {
        resources += ResourceMapping.List(this)
    }

    operator fun <Model : Any, Id : Any> Pair<ListResource<UserContext, Id, Model, *>, List<Subresource<UserContext, IdentifiedModel<Id, Model>, *>>>.unaryPlus() {
        resources += ResourceMapping.List(this.first, this.second)
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
