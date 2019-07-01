package edu.byu.uapi.utility.collections

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses


class TypeMap<Value : Any> private constructor(
    private val underlying: MutableMap<KClass<*>, Value>
) : MutableMap<KClass<*>, Value> by underlying {

    companion object {
        fun <Value : Any> create(): TypeMap<Value> = TypeMap(mutableMapOf())
        fun <Value : Any> create(map: Map<KClass<*>, Value>): TypeMap<Value> {
            val m = mutableMapOf<KClass<*>, Value>()
            m.putAll(map)
            return TypeMap(m)
        }
    }

    private val subtypeCache = mutableMapOf<KClass<*>, KClass<*>?>()

    private val lock = ReentrantReadWriteLock()

    @Suppress("UNCHECKED_CAST")
    fun getMatching(type: KClass<*>): Value? {
        lock.read {
            underlying[type]?.also { return it }
            val cachedSupertype = subtypeCache.getOrPut(type) { findMatchingSupertype(type) }

            return if (cachedSupertype != null) {
                underlying[cachedSupertype]
            } else {
                null
            }
        }
    }

    fun hasMatching(type: KClass<*>): Boolean {
        lock.read {
            if (type in underlying) return true
            if (type in subtypeCache) return true

            val foundSuper = findMatchingSupertype(type)
            return if (foundSuper != null) {
                subtypeCache[type] = foundSuper
                true
            } else {
                false
            }
        }
    }

    private fun findMatchingSupertype(type: KClass<*>): KClass<*>? {
        if (type == Any::class) {
            return null
        }
        val supers = type.superclasses
        if (supers.isEmpty()) {
            return null
        }
        // Let's find matches for our direct supertypes!
        supers.find { this.containsKey(it) }?.also { return it }
        // Well, stink. Let's find matches for THEIR supertypes!
        return supers.asSequence().map { findMatchingSupertype(it) }
            .filterNotNull()
            .firstOrNull()
    }

    override fun clear() {
        lock.write {
            subtypeCache.clear()
            underlying.clear()
        }
    }

    override fun put(
        key: KClass<*>,
        value: Value
    ): Value? {
        return lock.write {
            subtypeCache.clear()
            underlying.put(key, value)
        }
    }

    override fun putAll(from: Map<out KClass<*>, Value>) {
        return lock.write {
            subtypeCache.clear()
            underlying.putAll(from)
        }
    }

    override fun remove(key: KClass<*>): Value? {
        return lock.write {
            subtypeCache.clear()
            underlying.remove(key)
        }
    }
}
