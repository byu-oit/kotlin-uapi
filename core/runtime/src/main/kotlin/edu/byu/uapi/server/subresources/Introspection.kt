package edu.byu.uapi.server.subresources

import edu.byu.uapi.model.*
import edu.byu.uapi.server.resources.list.asIntrospectionLocation
import edu.byu.uapi.server.response.ValueResponseField
import edu.byu.uapi.server.response.getValuePropDefinition
import edu.byu.uapi.server.subresources.list.ListSubresource
import edu.byu.uapi.server.subresources.singleton.SingletonSubresource
import edu.byu.uapi.spi.introspection.IntrospectionContext
import edu.byu.uapi.spi.introspection.withLocation

internal fun introspect(
    runtime: SubresourceRuntime<*, *, *>,
    context: IntrospectionContext
): UAPISubresourceModel = when (runtime) {
    is SingletonSubresourceRuntime -> introspect(runtime, context)
    is ListSubresourceRuntime<*, *, *, *, *> -> introspect(runtime, context)
}

internal fun introspect(
    runtime: SingletonSubresourceRuntime<*, *, *>,
    context: IntrospectionContext
): UAPISingletonSubresourceModel {
    return context.withLocation(runtime.subresource.asIntrospectionLocation()) {
        val types = context.types

        val props = runtime.subresource.responseFields.associate { it.name to it.introspect(types) }

        UAPISingletonSubresourceModel(
            properties = props,
            update = runtime.subresource.updateMutation?.introspect(context),
            delete = runtime.subresource.deleteMutation?.introspect(context)
        )
    }
}

internal fun introspect(
    runtime: ListSubresourceRuntime<*, *, *, *, *>,
    context: IntrospectionContext
): UAPIListSubresourceModel {
    val types = context.types
    val props = runtime.subresource.responseFields.associate {
        it.name to it.introspect(types)
    }
    val keys = context.withLocation(runtime.subresource.idType) { introspectKeys(this, runtime.subresource)}

    val list = context.introspect(runtime.subresource.getListParamReader(types))

    return UAPIListSubresourceModel(
        keys = keys,
        properties = props,
        list = list,
        singularName = runtime.singleName,
        create = runtime.subresource.createOperation?.introspect(context),
        update = runtime.subresource.updateOperation?.introspect(context),
        delete = runtime.subresource.deleteOperation?.introspect(context)
    )
}

internal fun SingletonSubresource.Updatable<*, *, *, *>.introspect(context: IntrospectionContext): UAPIUpdateMutation {
    return context.withLocation(this::class) {
        UAPIUpdateMutation(
            inputSchema = UAPIInputSchema(),
            createsIfMissing = this is SingletonSubresource.Creatable<*, *, *, *>
        )
    }
}

internal fun SingletonSubresource.Deletable<*, *, *>.introspect(context: IntrospectionContext): UAPIDeleteMutation {
    return context.withLocation(this::class) {
        UAPIDeleteMutation()
    }
}

internal fun ListSubresource.Creatable<*, *, *, *, *>.introspect(context: IntrospectionContext): UAPICreateMutation {
    return context.withLocation(this::class) {
        UAPICreateMutation(
            inputSchema = UAPIInputSchema()
        )
    }
}

internal fun ListSubresource.Updatable<*, *, *, *, *>.introspect(context: IntrospectionContext): UAPIUpdateMutation {
    return context.withLocation(this::class) {
        UAPIUpdateMutation(
            inputSchema = UAPIInputSchema(),
            createsIfMissing = this is ListSubresource.CreatableWithId<*, *, *, *, *>
        )
    }
}

internal fun ListSubresource.Deletable<*, *, *, *>.introspect(context: IntrospectionContext): UAPIDeleteMutation {
    return context.withLocation(this::class) {
        UAPIDeleteMutation()
    }
}

internal fun introspectKeys(
    context: IntrospectionContext,
    resource: ListSubresource<*, *, *, *, *>
): List<String> {
    return context.withLocation(resource.idType) {
        val reader = resource.getIdReader(context.types)

        val idParams = reader.describe().idParams

        idParams.map { p ->
            val name = p.name
            val prop = resource.responseFields.find { it.name == name }
                ?: context.error(
                    "ID parameter $name is not listed in the resource's response properties.",
                    "Be sure that all ID parameters are listed in the response properties list.",
                    "If you're using a primitive ID type and the response property's name does not match the pattern `{resourceName}_id`, make sure you override the 'scalarIdParamName' in your resource"
                )
            if (!prop.key) {
                context.warn(
                    "ID parameter $name is not marked as a key in the resource's response properties.",
                    "Set `key = true` in the response definition for this property."
                )
            }
            if (prop !is ValueResponseField<*, *, *, *>) {
                context.warn(
                    "ID parameter $name is not a value property type.",
                    "ID parameters should be simple values types, not arrays or objects."
                )
            } else {
                if (types.getValuePropDefinition(prop.type).type != p.type) {
                    context.warn("ID parameter $name has a different type than the corresponding response property.")
                }
            }

            name
        }
    }
}
