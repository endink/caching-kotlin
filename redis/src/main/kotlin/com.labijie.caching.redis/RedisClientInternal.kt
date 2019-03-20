package com.labijie.caching.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.resource.ClientResources

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
class RedisClientInternal(val connection: StatefulRedisConnection<String, String>, val client: RedisClient, val serializer: String):AutoCloseable {
    override fun close() {
        connection.close()
        this.client.shutdown()
    }
}