package com.labijie.caching.redis.testing

import com.labijie.caching.redis.CacheDataSerializerRegistry
import com.labijie.caching.redis.serialization.JacksonCacheDataSerializer
import com.labijie.caching.redis.serialization.JsonSmileDataSerializer
import com.labijie.caching.redis.serialization.KryoCacheDataSerializer
import com.labijie.caching.redis.testing.model.ArgumentObject
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 *
 * @Author: Anders Xiao
 * @Date: 2021/12/10
 * @Description:
 */
class SerializeTester {
    private fun createTestData(): ArgumentObject {
        return ArgumentObject()
    }


    @Test
    fun serialize(){
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