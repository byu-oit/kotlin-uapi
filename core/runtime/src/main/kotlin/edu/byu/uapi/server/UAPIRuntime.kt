package edu.byu.uapi.server

import edu.byu.uapi.model.UAPIInfo
import edu.byu.uapi.model.UAPIModel
import edu.byu.uapi.server.inputs.DefaultTypeDictionary
import edu.byu.uapi.server.resources.list.ListResource
import edu.byu.uapi.server.resources.list.ListResourceRuntime
import edu.byu.uapi.server.resources.list.ResourceRuntime
import edu.byu.uapi.server.resources.list.asIntrospectionLocation
import edu.byu.uapi.server.subresources.Subresource
import edu.byu.uapi.server.types.IdentifiedModel
import edu.byu.uapi.server.types.ModelHolder
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.introspection.*
import edu.byu.uapi.spi.introspection.IntrospectionMessage.Severity.*
import edu.byu.uapi.spi.validation.ValidationEngine
import edu.byu.uapi.validation.hibernate.HibernateValidationEngine
import java.util.*
import kotlin.reflect.KClass

class UAPIRuntime<UserContext : Any>(
    options: Options<UserContext>
) : Introspectable<UAPIModel> {

    companion object {
        private val LOG = loggerFor<UAPIRuntime<*>>()
    }

    constructor(userContextFactory: UserContextFactory<UserContext>) : this(Options(userContextFactory))

    val userContextFactory = options.userContextFactory
    val typeDictionary = options.typeDictionary
    val validationEngine = options.validationEngine

    private val resources: MutableList<ResourceRuntime<UserContext, *, *>> = mutableListOf()

    fun <Id : Any, Model : Any> resource(
        resource: ListResource<UserContext, Id, Model, *>,
        init: SimpleResourceInit<UserContext, IdentifiedModel<Id, Model>>.() -> Unit = {}
    ) {
        SimpleResourceInitImpl(resource).also {
            init(it)
            addResource(it.createRuntime(this))
        }
    }

    fun <Id : Any, Model : Any, ContextNames : Enum<ContextNames>> resourceWithContexts(
        resource: ListResource<UserContext, Id, Model, *>,
        contextType: KClass<ContextNames>,
        init: ResourceWithContextsInit<UserContext, IdentifiedModel<Id, Model>, ContextNames>.() -> Unit = {}
    ) {
        ResourceWithContextsInitImpl(resource, contextType).also {
            init(it)
            addResource(it.createRuntime(this))
        }
    }

    private fun addResource(resource: ResourceRuntime<UserContext, *, *>) {
        resources.add(resource)
        val resourceModel = this.introspect(resource, "resource `${resource.name}`")
        _model = _model.copy(resources = _model.resources + resourceModel)
    }

    fun resources(): List<ResourceRuntime<UserContext, *, *>> = Collections.unmodifiableList(resources)

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

    private var _model: UAPIModel = buildModel()

    val model: UAPIModel
        get() = _model

    private fun buildModel(): UAPIModel {
        return introspect(this, "UAPI")
    }

    override fun introspect(context: IntrospectionContext): UAPIModel {
        return UAPIModel(
            info = UAPIInfo("UAPI Runtime", "0.1.0"),
            resources = resources.associate { context.introspect(it) }
        )
    }

    private fun <R> introspect(target: Introspectable<R>, logName: String): R {
        val context = DefaultIntrospectionContext(typeDictionary, IntrospectionLocation.Root)

        return try {
            target.introspect(context)
        } finally {
            val messages = context.messages
            if (messages.isEmpty()) {
                LOG.info("Built model for $logName with no errors, warnings, or suggestions.")
            } else {
                val errors = messages.count { it.severity == ERROR }
                val warnings = messages.count { it.severity == WARNING }
                val suggestions = messages.count { it.severity == SUGGESTION }

                LOG.warn(
                    "Got $errors errors, $warnings warnings, and $suggestions suggestions while building model for $logName:\n" +
                        messages.joinToString(separator = "\n", postfix = "\n") { it.toMultilineString("    ") }
                )
            }
        }
    }
}

class DefaultIntrospectionContext(
    override val types: TypeDictionary,
    override val location: IntrospectionLocation,
    private val _messages: MutableList<IntrospectionMessage> = mutableListOf()
) : IntrospectionContext {
    override val messages: List<IntrospectionMessage>
        get() = _messages

    override fun suggest(message: String, suggestions: List<String>) {
        _messages += IntrospectionMessage(IntrospectionMessage.Severity.SUGGESTION, this.location, message, suggestions)
    }

    override fun warn(message: String, suggestions: List<String>) {
        _messages += IntrospectionMessage(IntrospectionMessage.Severity.WARNING, this.location, message, suggestions)
    }

    override fun error(message: String, suggestions: List<String>): Nothing {
        _messages += IntrospectionMessage(ERROR, this.location, message, suggestions)
        throw IntrospectionException(
            this.location, message, suggestions
        )
    }

    override fun <R> introspect(target: Introspectable<R>): R {
        return withLocation(target.asIntrospectionLocation()) {
            target.introspect(this)
        }
    }

    override fun <R> withLocation(location: IntrospectionLocation, fn: IntrospectionContext.() -> R): R {
        val ctx = DefaultIntrospectionContext(
            this.types, this.location, this._messages
        )
        return ctx.fn()
    }
}

internal sealed class ResourceMapping<UserContext : Any, Model : Any, Parent : ModelHolder> {

    abstract fun toRuntime(
        typeDictionary: TypeDictionary,
        validationEngine: ValidationEngine
    ): ResourceRuntime<UserContext, Parent, *>

    data class List<UserContext : Any, Id : Any, Model : Any>(
        val resource: ListResource<UserContext, Id, Model, *>,
        val subresources: kotlin.collections.List<Subresource<UserContext, IdentifiedModel<Id, Model>, *>> = emptyList()
    ) : ResourceMapping<UserContext, Model, IdentifiedModel<Id, Model>>() {
        override fun toRuntime(
            typeDictionary: TypeDictionary,
            validationEngine: ValidationEngine
        ): ResourceRuntime<UserContext, IdentifiedModel<Id, Model>, *> {
            return ListResourceRuntime(
                this.resource,
                typeDictionary,
                validationEngine,
                subresources
            )
        }
    }
}

interface SimpleResourceInit<UserContext : Any, ParentType : ModelHolder> {
    fun subresource(subresource: Subresource<UserContext, ParentType, *>)
}

interface ResourceWithContextsInit<UserContext : Any, ParentType : ModelHolder, ContextNames : Enum<ContextNames>>
    : SimpleResourceInit<UserContext, ParentType> {

    fun subresource(pair: Pair<Subresource<UserContext, ParentType, *>, Set<ContextNames>>)

    fun subresource(
        subresource: Subresource<UserContext, ParentType, *>,
        vararg contexts: ContextNames
    )

    fun subresource(
        subresource: Subresource<UserContext, ParentType, *>,
        contexts: Iterable<ContextNames>
    )
}

internal interface InternalListResourceInit<UserContext : Any, Id : Any, Model : Any>
    : SimpleResourceInit<UserContext, IdentifiedModel<Id, Model>> {

    fun createRuntime(runtime: UAPIRuntime<UserContext>): ListResourceRuntime<UserContext, Id, Model, *>
}

internal class SimpleResourceInitImpl<UserContext : Any, Id : Any, Model : Any>(
    internal val resource: ListResource<UserContext, Id, Model, *>
) : InternalListResourceInit<UserContext, Id, Model>,
    SimpleResourceInit<UserContext, IdentifiedModel<Id, Model>> {

    internal val subresources: MutableList<Subresource<UserContext, IdentifiedModel<Id, Model>, *>> = mutableListOf()

    override fun subresource(subresource: Subresource<UserContext, IdentifiedModel<Id, Model>, *>) {
        subresources.add(subresource)
    }

    override fun createRuntime(runtime: UAPIRuntime<UserContext>): ListResourceRuntime<UserContext, Id, Model, *> {
        return ListResourceRuntime(
            resource,
            runtime.typeDictionary,
            runtime.validationEngine,
            subresources.toList()
        )
    }
}

internal class ResourceWithContextsInitImpl<UserContext : Any, Id : Any, Model : Any, ContextNames : Enum<ContextNames>>(
    internal val resource: ListResource<UserContext, Id, Model, *>,
    internal val contextType: KClass<ContextNames>
) : InternalListResourceInit<UserContext, Id, Model>,
    ResourceWithContextsInit<UserContext, IdentifiedModel<Id, Model>, ContextNames> {

    override fun subresource(pair: Pair<Subresource<UserContext, IdentifiedModel<Id, Model>, *>, Set<ContextNames>>) {
        this.subresource(pair.first, pair.second)
    }

    override fun subresource(
        subresource: Subresource<UserContext, IdentifiedModel<Id, Model>, *>,
        vararg contexts: ContextNames
    ) {
        this.subresource(subresource, contexts.toSet())
    }

    override fun subresource(
        subresource: Subresource<UserContext, IdentifiedModel<Id, Model>, *>,
        contexts: Iterable<ContextNames>
    ) {
        subresources[subresource] = contexts.toSet()
    }

    internal val subresources: MutableMap<Subresource<UserContext, IdentifiedModel<Id, Model>, *>, Set<ContextNames>> =
        mutableMapOf()

    override fun subresource(subresource: Subresource<UserContext, IdentifiedModel<Id, Model>, *>) {
        this.subresource(subresource, emptySet())
    }

    override fun createRuntime(runtime: UAPIRuntime<UserContext>): ListResourceRuntime<UserContext, Id, Model, *> {
        TODO("not implemented")
    }
}
