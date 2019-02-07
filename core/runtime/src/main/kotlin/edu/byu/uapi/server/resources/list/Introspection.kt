package edu.byu.uapi.server.resources.list

import edu.byu.uapi.model.*
import edu.byu.uapi.server.response.ValueResponseField
import edu.byu.uapi.server.response.getValuePropDefinition
import edu.byu.uapi.spi.introspection.IntrospectionContext
import edu.byu.uapi.spi.introspection.withLocation

internal fun introspect(
    runtime: ListResourceRuntime<*, *, *, *>,
    context: IntrospectionContext
): UAPIListResourceModel {
    val types = runtime.typeDictionary

    val props = runtime.resource.responseFields.associate { it.name to it.introspect(types) }

    val keys = context.withLocation(runtime.resource.idType) { introspectKeys(this, runtime.resource) }

    val list = context.introspect(runtime.resource.getListParamReader(types))

    return UAPIListResourceModel(
        properties = props,
        keys = keys,
        list = list,
        singularName = runtime.singleName,
        create = runtime.resource.createOperation?.introspect(context),
        update = runtime.resource.updateOperation?.introspect(context),
        delete = runtime.resource.deleteOperation?.introspect(context)
    )
}

internal fun ListResource.Creatable<*, *, *, *>.introspect(context: IntrospectionContext): UAPICreateMutation {
    return context.withLocation(this::class) {
        UAPICreateMutation(UAPIInputSchema())
    }
}

internal fun ListResource.Updatable<*, *, *, *>.introspect(context: IntrospectionContext): UAPIUpdateMutation {
    return context.withLocation(this::class) {
        UAPIUpdateMutation(
            inputSchema = UAPIInputSchema(),
            createsIfMissing = this is ListResource.CreatableWithId<*, *, *, *>
        )
    }
}

internal fun ListResource.Deletable<*, *, *>.introspect(context: IntrospectionContext): UAPIDeleteMutation {
    return context.withLocation(this::class) {
        UAPIDeleteMutation()
    }
}

internal fun introspectKeys(
    context: IntrospectionContext,
    resource: ListResource<*, *, *, *>
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
