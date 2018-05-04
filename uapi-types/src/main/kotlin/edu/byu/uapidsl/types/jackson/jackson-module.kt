package edu.byu.uapidsl.types.jackson

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.ser.Serializers
import kotlin.reflect.KClass

class JacksonUAPITypesModule : Module() {
    override fun getModuleName() = "university-api-datatypes"

    override fun version(): Version = Version.unknownVersion()

    override fun setupModule(context: SetupContext) {
        context.addSerializers(JacksonUAPISerializers(allSerializers))
    }
}

internal val allSerializers =
    fieldSerializers + linkSerializers + scalarSerializers

class JacksonUAPISerializers(
    private val serializers: Map<KClass<*>, JsonSerializer<*>>
) : Serializers.Base() {

    private val cache = mutableMapOf<JavaType, JsonSerializer<*>?>()

    override fun findSerializer(config: SerializationConfig, type: JavaType, beanDesc: BeanDescription): JsonSerializer<*>? {
        return cache.computeIfAbsent(type) {
            val found = serializers.keys.firstOrNull { k -> type.isTypeOrSubTypeOf(k.java) }
            if (found == null) {
                null
            } else {
                serializers[found]
            }
        }
    }
}

