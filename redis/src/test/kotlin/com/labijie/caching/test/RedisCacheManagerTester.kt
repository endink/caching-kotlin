/**
 * @author Anders Xiao
 * @date 2025-06-19
 */
package com.labijie.caching.test

import com.fasterxml.jackson.core.type.TypeReference
import com.labijie.caching.*
import com.labijie.caching.redis.CacheDataSerializerRegistry
import com.labijie.caching.redis.RedisCacheManager
import com.labijie.caching.redis.configuration.RedisCacheConfig
import com.labijie.caching.redis.configuration.RedisRegionOptions
import com.labijie.caching.redis.serialization.*
import kotlinx.serialization.Serializable
import java.time.Duration
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.test.*


open class RedisCacheManagerTester {
    private lateinit var redisCache: ICacheManager

    @BeforeTest
    @Throws(Exception::class)
    fun before() {
        CacheDataSerializerRegistry.registerSerializer(KryoCacheDataSerializer(null, kryoOptions = createKryoOptions()))
        CacheDataSerializerRegistry.registerSerializer(JacksonCacheDataSerializer())
        CacheDataSerializerRegistry.registerSerializer(KotlinJsonCacheDataSerializer())
        CacheDataSerializerRegistry.registerSerializer(KotlinProtobufCacheDataSerializer())
        this.redisCache = this.createCache()
        this.redisCache.clear()
    }

    @AfterTest
    @Throws(Exception::class)
    fun after() {
        CacheDataSerializerRegistry.clear()
        this.redisCache.clear()
    }


    open protected fun createKryoOptions(): KryoOptions {
        return KryoOptions().apply {
            this.registerClass(101, TestData::class)
        }
    }

    protected val isKotlinSerialization
        get() = getSerializerName().startsWith("kotlin-")

    private fun createCache(): RedisCacheManager {
        val redisConfig = RedisCacheConfig()
        redisConfig.regions["default"] = RedisRegionOptions(url = "${TestingServer.serverUri}/0")
        redisConfig.regions["region1"] = RedisRegionOptions(url = "${TestingServer.serverUri}/1")
        redisConfig.regions["region2"] = RedisRegionOptions(url = "${TestingServer.serverUri}/2")
        redisConfig.regions["region3"] = RedisRegionOptions(url = "${TestingServer.serverUri}/3")

        redisConfig.regions["a"] = RedisRegionOptions(url = "${TestingServer.serverUri}/4")
        redisConfig.regions["b"] = RedisRegionOptions(url = "${TestingServer.serverUri}/5")
        redisConfig.regions["c"] = RedisRegionOptions(url = "${TestingServer.serverUri}/6")
        redisConfig.regions["d"] = RedisRegionOptions(url = "${TestingServer.serverUri}/7")
        redisConfig.regions["e"] = RedisRegionOptions(url = "${TestingServer.serverUri}/8")
        redisConfig.regions["f"] = RedisRegionOptions(url = "${TestingServer.serverUri}/9")

        redisConfig.regions.values.forEach { it.serializer = this.getSerializerName() }


        return RedisCacheManager(redisConfig)
    }

    protected open fun getSerializerName(): String = JacksonCacheDataSerializer.NAME

    @Test
    fun testSerializer() {
        val serializer = CacheDataSerializerRegistry.getSerializer(getSerializerName())
        val data = newData()

        val ktype: KType = typeOf<TestData>()
        val bytes = serializer.serializeData(data, ktype)
        val decoded = serializer.deserializeData(ktype, bytes)

        assertEquals(data, decoded)
    }

    /**
     * Method: get(String key, String region)
     */
    @Test
    @Throws(Exception::class)
    fun testGet() {
        assertNull(redisCache.get<String>("a", "b"), "Get should return null when the value does not exist.")
        assertNull(redisCache.get<String>("b", "a"), "Get should return null when the value does not exist.")
        assertNull(redisCache.get<String>("a", ""), "Get should return null when the value does not exist.")
        assertNull(redisCache.get<String>("a", null as String?), "Get should return null when the value does not exist.")

        val `val` = newData()
        redisCache.set("a", `val`, null, TimePolicy.Absolute, "b")
        val vv = redisCache.get<TestData>("a", "b")
        assertEquals(`val`, vv, "The value returned by get does not match the value set by set.")
    }

    /**
     * Method: set(String key, Object data, Long timeoutMilliseconds, String region, boolean useSlidingExpiration)
     */
    @Test
    @Throws(Exception::class)
    fun testSet() {
        val `val` = newData()
        redisCache.set("a", `val`, null, TimePolicy.Absolute, "region1")
        redisCache.set("b", `val`, 5000L, TimePolicy.Absolute, "region2")
        redisCache.set("c", `val`, 5000L, TimePolicy.Sliding, null)
        redisCache.set("d", `val`, null, TimePolicy.Sliding, null)
        redisCache.set("e", `val`, null, TimePolicy.Sliding, "")

        assertEquals(`val`, redisCache.get<TestData>("a", "region1"), "The value returned by get does not match the value set by set.")
        assertEquals(`val`, redisCache.get<TestData>("b", "region2"), "The value returned by get does not match the value set by set.")
        assertEquals(`val`, redisCache.get<TestData>("c", null as String?), "The value returned by get does not match the value set by set.")
        assertEquals(`val`, redisCache.get<TestData>("d", null as String?), "The value returned by get does not match the value set by set.")
        assertEquals(`val`, redisCache.get<TestData>("e", ""), "The value returned by get does not match the value set by set.")
    }

    /**
     * Method: remove(String key, String region)
     */
    @Test
    @Throws(Exception::class)
    fun testRemove() {
        val `val` = newData()
        redisCache.set("a", `val`, null, TimePolicy.Absolute, "region1")
        redisCache.set("b", `val`, 5000L, TimePolicy.Absolute, "region2")
        redisCache.set("c", `val`, 5000L, TimePolicy.Sliding, null)
        redisCache.set("d", `val`, null, TimePolicy.Sliding, null)
        redisCache.set("e", `val`, null, TimePolicy.Sliding, "")

        redisCache.remove("a", "region1")
        redisCache.remove("b", "region2")
        redisCache.remove("c", null)
        redisCache.remove("d", null)
        redisCache.remove("e", "")

        assertNull(redisCache.get("a", TestData::class.java, "region1"), "Remove did not take effect.")
        assertNull(redisCache.get("b", TestData::class.java, "region2"), "Remove did not take effect.")
        assertNull(redisCache.get("c", TestData::class.java, null as String?), "Remove did not take effect.")
        assertNull(redisCache.get("d", TestData::class.java, null as String?), "Remove did not take effect.")
        assertNull(redisCache.get("e", TestData::class.java, ""), "Remove did not take effect.")
    }

    @Test
    fun testGeneric() {
        val lst = listOf(newData())
        redisCache.set("b", lst, 5000L, TimePolicy.Absolute, "region2")
        redisCache.get("b", getGenericType(List::class.java, TestData::class.java))
        println(lst)
        assert(lst.isNotEmpty())
    }

    /**
     * Method: clearRegion(String region)
     */
    @Test
    @Throws(Exception::class)
    fun testClearRegion() {
        val `val` = newData()
        redisCache.set("a", `val`, null, TimePolicy.Absolute, "region1")
        redisCache.set("b", `val`, 5000L, TimePolicy.Absolute, "region2")
        redisCache.set("c", `val`, 5000L, TimePolicy.Sliding)
        redisCache.set("d", `val`, null, TimePolicy.Sliding)
        redisCache.set("e", `val`, null, TimePolicy.Sliding, "")
        redisCache.set("f", `val`, 5000L, TimePolicy.Absolute, "region3")

        redisCache.clearRegion("region1")
        redisCache.clearRegion("region2")

        assertNull(redisCache.get<TestData>("a", "region1"), "clearRegion did not take effect.")
        assertNull(redisCache.get<TestData>("b", "region2"), "clearRegion did not take effect.")
        assertNotNull(redisCache.get<TestData>("c", null as String?), "clearRegion incorrectly cleared extra region.")
        assertNotNull(redisCache.get<TestData>("d", null as String?), "clearRegion incorrectly cleared extra region.")
        assertNotNull(redisCache.get<TestData>("e", ""), "clearRegion incorrectly cleared extra region.")
        assertNotNull(redisCache.get<TestData>("f", "region3"), "clearRegion incorrectly cleared extra region.")
    }

    /**
     * Method: clear()
     */
    @Test
    @Throws(Exception::class)
    fun testClear() {
        val `val` = newData()
        redisCache.set("a", `val`, null, TimePolicy.Absolute, "region1")
        redisCache.set("b", `val`, 5000L, TimePolicy.Absolute, "region2")
        redisCache.set("c", `val`, 5000L, TimePolicy.Sliding)
        redisCache.set("d", `val`, null, TimePolicy.Sliding)
        redisCache.set("e", `val`, null, TimePolicy.Sliding, "")

        redisCache.clear()

        assertNull(redisCache.get("a", TestData::class.java, "region1"), "clear did not take effect.")
        assertNull(redisCache.get("b", TestData::class.java, "region2"), "clear did not take effect.")
        assertNull(redisCache.get("c", TestData::class.java, null as String?), "clear did not take effect.")
        assertNull(redisCache.get("d", TestData::class.java, null as String?), "clear did not take effect.")
        assertNull(redisCache.get("e", TestData::class.java, ""), "clear did not take effect.")
    }

    @Test
    @Throws(Exception::class)
    fun testList() {
        val list = listOf(newData(), newData(), newData())

        redisCache.set("list-test", list, 5000L)

        val tr = object : TypeReference<List<TestData>>() {}
        val value = if(isKotlinSerialization) redisCache.get<List<TestData>>("list-test") else redisCache.get("list-test", tr.type)
        assertNotNull(value)

        val listData = value as List<*>
        assertEquals(3, listData.size)

        assertEquals(list, listData)
    }

    @Test
    @Throws(Exception::class)
    fun testMap() {
        val map = mapOf(
            "123" to newData(),
            "234" to newData(),
            "345" to newData()
        )

        redisCache.set("list-test", map, 5000L)

        val tr = object : TypeReference<Map<String, TestData>>() {}
        val value = if(isKotlinSerialization) redisCache.get<Map<String, TestData>>("list-test") else redisCache.get("list-test", tr.type)

        assertNotNull(value)

        val listData = value as Map<*, *>
        assertEquals(3, listData.size)

        assertEquals(map.toList(), listData.toList())
    }

    @Test
    @Throws(Exception::class)
    fun testMapGetOrSet() {
        val map: Map<String, TestData> = mapOf(
            "123" to newData(),
            "234" to newData(),
            "345" to newData()
        )

        val map2: Map<String, TestData> = mapOf(
            "123" to newData(),
            "234" to newData(),
        )

        val data = redisCache.getOrSet("list-test", null, Duration.ofSeconds(60)) { map }

        val data2 = redisCache.getOrSet("list-test", Duration.ofSeconds(60)) {
            map2
        }

        assertSame(map, data)
        assertNotNull(data)
        assertNotNull(data2)
        assertEquals(data.toList(), data2.toList())

        val tr = object : TypeReference<Map<String, TestData>>() {}
        val value = if(isKotlinSerialization) redisCache.get<Map<String, TestData>>("list-test", ) else redisCache.get("list-test", tr.type)
        assertNotNull(value)

        val listData = value as Map<*, *>
        assertEquals(3, listData.size)

        assertEquals(map.toList(), listData.toList())
    }

    @Test
    fun testSetMulti() {
        val keyPrefix = "multi-key-test"
        val region = "region1"

        // Prepare multiple keys and values
        val testValues = mapOf(
            "${keyPrefix}-1" to ICacheItem.of(newData()),
            "${keyPrefix}-2" to ICacheItem.of(newData()),
            "${keyPrefix}-3" to ICacheItem.of(newData())
        )

        // Save all key-values with expiration and absolute time policy
        redisCache.setMulti(
            keyAndValues = testValues,
            expireMills = 2000L,
            timePolicy = TimePolicy.Absolute,
            region = region
        )

        // Validate they are stored correctly
        for ((key, value) in testValues) {
            val cached = redisCache.get<TestData>(key, region)
            assertNotNull(cached)
            assertEquals(value.getData(), cached, "Cached value for key '$key' does not match the original.")
        }

        // Wait for 3 seconds to test expiration
        Thread.sleep(3000)

        // Validate values have expired
        for ((key, _) in testValues) {
            val cached = redisCache.get(key, TestData::class.java, region)
            assertNull(cached, "Cached value for key '$key' should have expired.")
        }
    }

    @Test
    open fun testRemoveMulti() {
        val keyPrefix = "multi-key-test"
        val region = "region1"

        // Prepare multiple keys and values
        val testValues = mapOf(
            "${keyPrefix}-1" to ICacheItem.of(newData()),
            "${keyPrefix}-2" to ICacheItem.of(newData()),
            "${keyPrefix}-3" to ICacheItem.of(newData())
        )

        // Save all key-values with expiration and absolute time policy
        redisCache.setMulti(
            keyAndValues = testValues,
            expireMills = 600 * 1000,
            timePolicy = TimePolicy.Absolute,
            region = region
        )

        for ((key, value) in testValues) {
            val cached = if(isKotlinSerialization) redisCache.get<TestData>(key, region) else redisCache.get(key, TestData::class.java, region)
            assertEquals(value.getData(), cached, "Cached value for key '$key' does not match the original.")
        }

        val removeKeys  = mutableSetOf(*testValues.keys.toTypedArray())
        removeKeys.add("not existed")
        val count = redisCache.removeMulti(removeKeys, region)

        assertEquals(testValues.size, count)

        for ((key, _) in testValues) {
            val cached = redisCache.get(key, TestData::class.java, region)
            assertNull(cached, "Cached value for key '$key' should have removed.")
        }
    }


    @Serializable
    data class TestData(
        val id: Int = 0,
        val name: String = "",
        val age: Int = 0,
        val email: String = "",
        val isActive: Boolean = false,
        val score: Double = 0.0,
        val ratio: Float = 0f,
        val timestamp: Long = 0L,
        val tags: List<String> = emptyList(),
        val metadata: Map<String, String> = emptyMap(),
        val optionalField: String? = null
    )

    protected fun newData(): TestData {
        return TestData(
            id = (1..1000).random(),
            name = listOf("Alice", "Bob", "Charlie", "Diana").random(),
            age = (18..60).random(),
            email = "${('a'..'z').shuffled().take(5).joinToString("")}@example.com",
            isActive = listOf(true, false).random(),
            score = (50..100).random() + Math.random(), // Double
            ratio = (0..100).random() / 100f,            // Float
            timestamp = System.currentTimeMillis(),
            tags = listOf("kotlin", "proto", "test", "sample").shuffled().take(2),
            metadata = mapOf("env" to "dev", "version" to "1.${(0..5).random()}"),
            optionalField = if ((0..1).random() == 0) null else "optional"
        )

    }

}
