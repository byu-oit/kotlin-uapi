package edu.byu.uapidsl.types.jackson

import com.fasterxml.jackson.databind.JsonSerializer
import edu.byu.uapidsl.types.LinkMethod
import kotlin.reflect.KClass

internal val linkSerializers = mapOf<KClass<*>, JsonSerializer<*>>(
    LinkMethod::class to LinkMethodSerializer
)

internal object LinkMethodSerializer : ApiEnumSerializer<LinkMethod>(LinkMethod::class)


