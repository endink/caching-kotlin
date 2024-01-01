package com.labijie.caching.redis.serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Registration
import com.esotericsoftware.kryo.Serializer
import com.labijie.caching.CacheException
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-08
 */
class KryoOptions {

    data class KryoRegistry<T>(val id: Int, val clazz: Class<T>, val serializer: Serializer<T>?)

    private val registeredClasses:MutableMap<Int, KryoRegistry<*>> = mutableMapOf()

    var writeBufferSizeBytes = 1024 * 4
    var poolSize = -1
    var isRegistrationRequired:Boolean = false
    var warnUnregisteredClasses:Boolean = false

    fun getRegistry(): Array<KryoRegistry<*>> {
        return registeredClasses.values.toTypedArray()
    }


    fun <T : Any> registerClass(id: Int, type: KClass<T>, serializer: Serializer<T>? = null) {
        if (id <= 100) {
            throw IllegalArgumentException("Kryo register class id must be greater than 100 ( start with 101 ), want ot register: ${id}: ${type.simpleName}")
        }
        val existed = registeredClasses.getOrDefault(id, null)
        existed?.let {
            throw CacheException("Class registered to kryo with id '$id' was existed. ( existed: ${existed.clazz.simpleName}, registering: ${type.simpleName} )")
        }
        registeredClasses[id] = KryoRegistry(id, type.java, serializer)
    }
}