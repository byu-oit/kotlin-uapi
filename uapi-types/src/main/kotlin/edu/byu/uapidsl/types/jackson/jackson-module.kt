package edu.byu.uapidsl.types.jackson

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.ser.Serializers
import kotlin.reflect.KClass

class JacksonUAPITypesModule : Module() {
    override fun getModuleName() = "university-api-datatypes"

    override fun version(): Version = Version.unknownVersion()

    override fun setupModule(context: SetupContext) {
        context.setNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

        context.addSerializers(JacksonUAPISerializers(allSerializers))

        allMixins.forEach { context.setMixInAnnotations(it.forType.java, it.mixin.java) }
    }
}

data class Mixin(
    val forType: KClass<*>,
    val mixin: KClass<*>
)

inline fun <reified For, reified Mixin> mixin() = Mixin(For::class, Mixin::class)

internal val allSerializers =
    fieldSerializers + linkSerializers

internal val allMixins: List<Mixin> = responseMixins

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

@JacksonAnnotationsInside
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy::class)
annotation class UAPIJackson
