package com.labijie.caching.redis

import com.labijie.caching.redis.codec.RedisValue
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
class RedisClientInternal(
    val region:String,
    val connection: StatefulRedisConnection<String, RedisValue>,
    private val client: RedisClient,
    private val serializerName: String):AutoCloseable {

    val serializer by lazy {
        CacheDataSerializerRegistry.getSerializer(serializerName)
    }
    override fun close() {
        connection.close()
        this.client.shutdown()
    }
}