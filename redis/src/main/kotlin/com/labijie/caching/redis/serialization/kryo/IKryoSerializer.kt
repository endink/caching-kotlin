/**
 * @author Anders Xiao
 * @date 2024-06-18
 */
package com.labijie.caching.redis.serialization.kryo


interface IKryoSerializer {
    fun serialize(data: Any): ByteArray

    fun deserialize(data: ByteArray, clazz: Class<*>): Any
}