package edu.byu.uapi.spi.introspection

import edu.byu.uapi.model.jsonschema07.Schema
import kotlin.reflect.KClass

interface SchemaGenerator {
    fun generateSchemaFor(type: KClass<*>): Schema
}
