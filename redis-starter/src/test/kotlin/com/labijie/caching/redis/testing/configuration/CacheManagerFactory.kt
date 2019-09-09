package com.labijie.caching.redis.testing.configuration

import com.labijie.caching.redis.ICacheDataSerializer
import com.labijie.caching.redis.RedisCacheManager
import com.labijie.caching.redis.configuration.RedisCacheConfig
import com.labijie.caching.redis.configuration.RedisRegionOptions
import com.labijie.caching.redis.serialization.JacksonCacheDataSerializer
import com.labijie.caching.redis.serialization.KryoCacheDataSerializer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-09
 */
abstract class CacheManagerFactory(private val serializerName: String){
    fun createCacheManager(): RedisCacheManager {

        val config = RedisCacheConfig().apply {
            this.regions["default"] = RedisRegionOptions("redis://localhost:6379/1", serializer = serializerName)
        }
        return RedisCacheManager(config)
    }
}

class JacksonCacheManagerFactory(): CacheManagerFactory(JacksonCacheDataSerializer.NAME)

class KryoCacheManagerFactory():CacheManagerFactory(KryoCacheDataSerializer.NAME)