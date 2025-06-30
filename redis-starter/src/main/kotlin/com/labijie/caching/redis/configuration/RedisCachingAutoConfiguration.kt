package com.labijie.caching.redis.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.smile.databind.SmileMapper
import com.labijie.caching.ICacheManager
import com.labijie.caching.ScopedCacheManager
import com.labijie.caching.configuration.CachingAutoConfiguration
import com.labijie.caching.redis.CacheDataSerializerRegistry
import com.labijie.caching.redis.ICacheDataSerializer
import com.labijie.caching.redis.RedisCacheManager
import com.labijie.caching.redis.serialization.JacksonCacheDataSerializer
import com.labijie.caching.redis.serialization.JsonSmileDataSerializer
import com.labijie.caching.redis.serialization.KryoCacheDataSerializer
import com.labijie.caching.redis.serialization.KryoOptions
import com.labijie.caching.kryo.IKryoSerializer
import com.labijie.caching.redis.customization.IKotlinJsonSerializationCustomizer
import com.labijie.caching.redis.customization.IKotlinProtobufSerializationCustomizer
import com.labijie.caching.redis.serialization.KotlinJsonCacheDataSerializer
import com.labijie.caching.redis.serialization.KotlinProtobufCacheDataSerializer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-26
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(CachingAutoConfiguration::class)
@ConditionalOnMissingBean(ICacheManager::class)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnProperty(name = ["infra.caching.provider"], havingValue = "redis", matchIfMissing = true)
class RedisCachingAutoConfiguration {

    @Bean
    @ConfigurationProperties("infra.caching.redis")
    fun redisCacheConfig(): RedisCacheConfig {
        return RedisCacheConfig()
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = ["kotlinx.serialization.json.Json"])
    protected class KotlinJsonCacheDataSerializerAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean(JacksonCacheDataSerializer::class)
        fun kotlinJsonCacheDataSerializer(customizers: ObjectProvider<IKotlinJsonSerializationCustomizer>): KotlinJsonCacheDataSerializer {
            return KotlinJsonCacheDataSerializer(customizers.orderedStream().toList())
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = ["kotlinx.serialization.json.Json"])
    protected class KotlinProtobufCacheDataSerializerAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean(JacksonCacheDataSerializer::class)
        fun kotlinProtobufCacheDataSerializer(customizers: ObjectProvider<IKotlinProtobufSerializationCustomizer>): KotlinProtobufCacheDataSerializer {
            return KotlinProtobufCacheDataSerializer(customizers)
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = ["com.fasterxml.jackson.databind.ObjectMapper"])
    protected class JacksonCacheDataSerializerAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean(JacksonCacheDataSerializer::class)
        fun jacksonCacheDataSerializer(
            @Autowired(required = false) objectMapper: ObjectMapper?,
            customizers: ObjectProvider<IJacksonCacheDataSerializerCustomizer>
        ): JacksonCacheDataSerializer {
            val mapper = objectMapper ?: JacksonCacheDataSerializer.createObjectMapper()
            customizers.orderedStream().forEach {
                it.customize(mapper)
            }
            return JacksonCacheDataSerializer(objectMapper)
        }
    }



    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = ["com.esotericsoftware.kryo.Kryo"])
    protected class KryoCachingSerializerAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean(KryoCacheDataSerializer::class)
        fun kryoCacheDataSerializer(
            @Autowired(required = false) kryoSerializer: IKryoSerializer?,
            customizers: ObjectProvider<IKryoCacheDataSerializerCustomizer>
        ): KryoCacheDataSerializer {
            val kryoOptions = KryoOptions()
            customizers.orderedStream().forEach {
                it.customize(kryoOptions)
            }
            return KryoCacheDataSerializer(kryoSerializer, kryoOptions)
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = ["com.fasterxml.jackson.dataformat.smile.databind.SmileMapper"])
    protected class JacksonSmileCacheDataSerializerAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean(JsonSmileDataSerializer::class)
        fun jsonSmileDataSerializer(
            @Autowired(required = false) mapper: SmileMapper?,
            customizers: ObjectProvider<IJsonSmileCacheDataSerializerCustomizer>
        ): JsonSmileDataSerializer {
            val smileMapper = mapper ?: JsonSmileDataSerializer.createObjectMapper()
            customizers.orderedStream().forEach {
                it.customize(smileMapper)
            }
            return JsonSmileDataSerializer(smileMapper)
        }
    }

    @Bean
    fun redisCacheManager(
        serializers: ObjectProvider<ICacheDataSerializer>,
        config: RedisCacheConfig,

        ): ScopedCacheManager {
        serializers.orderedStream().forEach {
            CacheDataSerializerRegistry.registerSerializer(it)
        }
        val innerCache = RedisCacheManager(config)
        return ScopedCacheManager(innerCache)
    }
}