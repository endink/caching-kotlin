package com.labijie.caching.redis.configuration

import com.labijie.caching.ICacheManager
import com.labijie.caching.configuration.CachingAutoConfiguration
import com.labijie.caching.memory.MemoryCacheManager
import com.labijie.caching.memory.MemoryCacheOptions
import com.labijie.caching.redis.CacheDataSerializerRegistry
import com.labijie.caching.redis.ICacheDataSerializer
import com.labijie.caching.redis.RedisCacheManager
import com.labijie.caching.redis.serialization.JacksonCacheDataSerializer
import com.labijie.caching.redis.serialization.KryoCacheDataSerializer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-26
 */
@Configuration
@AutoConfigureBefore(CachingAutoConfiguration::class)
@ConditionalOnMissingBean(ICacheManager::class)
class RedisCachingAutoConfiguration {

    @Bean
    @ConfigurationProperties("infra.caching.redis")
    fun redisCacheConfig(): RedisCacheConfig {
        return RedisCacheConfig()
    }

    @Bean
    @ConditionalOnMissingBean(JacksonCacheDataSerializer::class)
    fun jacksonCacheDataSerializer(): JacksonCacheDataSerializer {
        return JacksonCacheDataSerializer()
    }

    @Bean
    @ConditionalOnMissingBean(KryoCacheDataSerializer::class)
    fun kryoCacheDataSerializer(customizers: ObjectProvider<KryoCacheDataSerializerCustomizer>): KryoCacheDataSerializer {
        customizers.orderedStream().forEach {
            it.configure()
        }
        val options = KryoCacheDataSerializerCustomizer.kryoOptions
        return KryoCacheDataSerializer(options)
    }

    @Bean
    fun redisCacheManager(
        serializers: ObjectProvider<ICacheDataSerializer>,
        config: RedisCacheConfig
    ): RedisCacheManager {
        serializers.orderedStream().forEach {
            CacheDataSerializerRegistry.registerSerializer(it)
        }
        return RedisCacheManager(config)
    }
}