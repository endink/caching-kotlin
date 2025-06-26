package com.labijie.caching.test

import com.labijie.caching.redis.ICacheDataSerializer
import com.labijie.caching.redis.serialization.JacksonCacheDataSerializer
import com.labijie.caching.redis.serialization.KryoCacheDataSerializer
import com.labijie.caching.redis.serialization.KryoOptions
import java.util.UUID
import kotlin.random.Random
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2025/6/26
 */
class DataSerializationTester {

    data class TestData(
        var intValue: Int = Random.nextInt(),
        var stringValue: String = UUID.randomUUID().toString()
    )

    fun testCore(serializer: ICacheDataSerializer) {
        val testData = TestData()

        val data = serializer.serializeData(testData, typeOf<TestData>())

        val decoded = serializer.deserializeData(TestData::class.java, data)

        assertEquals(testData,decoded)
    }

    @Test
    fun testJackson() {
        testCore(JacksonCacheDataSerializer())
    }

    @Test
    fun testKryo() {
        testCore(KryoCacheDataSerializer(null, kryoOptions = KryoOptions()))
    }
}