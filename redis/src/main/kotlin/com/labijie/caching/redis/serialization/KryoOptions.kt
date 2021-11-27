package com.labijie.caching.redis.serialization

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
    private val registeredClasses:MutableMap<Int, Registration> = mutableMapOf()

    var writeBufferSizeBytes = 1024 * 4
    var poolSize = -1
    var isRegistrationRequired:Boolean = false
    var warnUnregisteredClasses:Boolean = false

    fun getRegistry(): Array<Registration> {
        return registeredClasses.values.toTypedArray()
    }

    fun <T : Any> registerClass(id: Int, type: KClass<T>, serializer: Serializer<T>? = null) {
        if (id <= 100) {
            throw IllegalArgumentException("Kryo register class id must be greater than 100 ( start with 101 )")
        }
        val existed = registeredClasses.getOrDefault(id, null)
        if (existed != null) {
            throw CacheException("Class registered to kryo with id '$id' was existed. ( existed: ${existed.type.simpleName}, registering: ${type.simpleName} )")
        }
        registeredClasses[id] = Registration(type.java, serializer, id)
    }
}