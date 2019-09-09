package com.labijie.caching.redis.testing.configuration

import com.labijie.caching.ICacheManager
import com.labijie.caching.redis.CacheDataSerializerRegistry
import com.labijie.caching.redis.ICacheDataSerializer
import com.labijie.caching.redis.RedisCacheManager
import com.labijie.caching.redis.configuration.RedisCacheConfig
import com.labijie.caching.redis.configuration.RedisRegionOptions
import com.labijie.caching.redis.serialization.KryoCacheDataSerializer
import com.labijie.caching.redis.testing.bean.SimpleTestingBean
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-25
 */

@Configuration
@EnableAspectJAutoProxy
class TestConfiguration {
    @Bean
    fun simpleTestingBean(): SimpleTestingBean {
        return SimpleTestingBean()
    }

    @Bean
    fun cacheManager(factory:CacheManagerFactory):ICacheManager{
        return factory.createCacheManager()
    }
}