package com.labijie.caching.redis.testing

import com.labijie.caching.redis.CacheDataSerializerRegistry
import com.labijie.caching.redis.configuration.RedisCachingAutoConfiguration
import com.labijie.caching.redis.serialization.JacksonCacheDataSerializer
import com.labijie.caching.redis.serialization.JsonSmileDataSerializer
import com.labijie.caching.redis.serialization.KryoCacheDataSerializer
import com.labijie.caching.redis.testing.configuration.TestConfiguration
import com.labijie.caching.redis.testing.model.ArgumentObject
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 *
 * @Author: Anders Xiao
 * @Date: 2021/12/10
 * @Description:
 */
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [RedisCachingAutoConfiguration::class, TestConfiguration::class])
class SerializeTester {
    private fun createTestData(): ArgumentObject {
        return ArgumentObject()
    }


    @Test
    fun serializeSizeCompare(){
        val data = createTestData()

        val smile = CacheDataSerializerRegistry.getSerializer(JsonSmileDataSerializer.NAME)
        val kryo = CacheDataSerializerRegistry.getSerializer(KryoCacheDataSerializer.NAME)
        val json = CacheDataSerializerRegistry.getSerializer(JacksonCacheDataSerializer.NAME)

        val smileData = smile.serializeData(data)
        val kryoData = kryo.serializeData(data)
        val jsonData = json.serializeData(data)

        assertTrue { smileData.size < jsonData.size }
        assertTrue { kryoData.size < jsonData.size }
    }

}