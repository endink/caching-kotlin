/**
 * @author Anders Xiao
 * @date 2025-06-16
 */
package com.labijie.caching.redis.aot

import com.labijie.caching.redis.configuration.RedisCachingAutoConfiguration
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar


class RedisRuntimeHintsRegistrar : RuntimeHintsRegistrar {
    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        hints.reflection().registerType(RedisCachingAutoConfiguration::class.java)
        hints.reflection().registerType(Any::class.java)
        hints.reflection().registerType(StatefulRedisClusterConnection::class.java)
    }
}