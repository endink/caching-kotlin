/**
 * @author Anders Xiao
 * @date 2025-06-19
 */
package com.labijie.caching.test

import com.fasterxml.jackson.core.type.TypeReference
import com.labijie.caching.ICacheManager
import com.labijie.caching.TimePolicy
import com.labijie.caching.getGenericType
import com.labijie.caching.getOrSet
import com.labijie.caching.redis.RedisCacheManager
import com.labijie.caching.redis.configuration.RedisCacheConfig
import com.labijie.caching.redis.configuration.RedisRegionOptions
import com.labijie.caching.redis.serialization.JacksonCacheDataSerializer
import kotlin.test.*
import java.time.Duration
import java.util.*
import kotlin.random.Random


open class RedisCacheManagerTester {
    private lateinit var redisCache: ICacheManager

    @BeforeTest
    @Throws(Exception::class)
    fun before() {
        this.redisCache = this.createCache()
        this.redisCache.clear()
    }

    @AfterTest
    @Throws(Exception::class)
    fun after() {
        this.redisCache.clear()
    }

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

    /**
     * Method: get(String key, String region)
     */
    @Test
    @Throws(Exception::class)
    fun testGet() {
        assertNull(redisCache.get("a", String::class.java, "b"), "Get should return null when the value does not exist.")
        assertNull(redisCache.get("b", String::class.java, "a"), "Get should return null when the value does not exist.")
        assertNull(redisCache.get("a", String::class.java, ""), "Get should return null when the value does not exist.")
        assertNull(redisCache.get("a", String::class.java, null as String?), "Get should return null when the value does not exist.")

        val `val` = TestData()
        redisCache.set("a", `val`, null, TimePolicy.Absolute, "b")
        val vv = redisCache.get("a", TestData::class.java, "b")
        assertEquals(`val`, vv, "The value returned by get does not match the value set by set.")
    }

    /**
     * Method: set(String key, Object data, Long timeoutMilliseconds, String region, boolean useSlidingExpiration)
     */
    @Test
    @Throws(Exception::class)
    fun testSet() {
        val `val` = TestData()
        redisCache.set("a", `val`, null, TimePolicy.Absolute, "region1")
        redisCache.set("b", `val`, 5000L, TimePolicy.Absolute, "region2")
        redisCache.set("c", `val`, 5000L, TimePolicy.Sliding, null)
        redisCache.set("d", `val`, null, TimePolicy.Sliding, null)
        redisCache.set("e", `val`, null, TimePolicy.Sliding, "")

        assertEquals(`val`, redisCache.get("a", TestData::class.java, "region1"), "The value returned by get does not match the value set by set.")
        assertEquals(`val`, redisCache.get("b", TestData::class.java, "region2"), "The value returned by get does not match the value set by set.")
        assertEquals(`val`, redisCache.get("c", TestData::class.java, null as String?), "The value returned by get does not match the value set by set.")
        assertEquals(`val`, redisCache.get("d", TestData::class.java, null as String?), "The value returned by get does not match the value set by set.")
        assertEquals(`val`, redisCache.get("e", TestData::class.java, ""), "The value returned by get does not match the value set by set.")
    }

    /**
     * Method: remove(String key, String region)
     */
    @Test
    @Throws(Exception::class)
    fun testRemove() {
        val `val` = TestData()
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
        val lst = listOf(TestData())
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
        val `val` = TestData()
        redisCache.set("a", `val`, null, TimePolicy.Absolute, "region1")
        redisCache.set("b", `val`, 5000L, TimePolicy.Absolute, "region2")
        redisCache.set("c", `val`, 5000L, TimePolicy.Sliding)
        redisCache.set("d", `val`, null, TimePolicy.Sliding)
        redisCache.set("e", `val`, null, TimePolicy.Sliding, "")
        redisCache.set("f", `val`, 5000L, TimePolicy.Absolute, "region3")

        redisCache.clearRegion("region1")
        redisCache.clearRegion("region2")

        assertNull(redisCache.get("a", TestData::class.java, "region1"), "clearRegion did not take effect.")
        assertNull(redisCache.get("b", TestData::class.java, "region2"), "clearRegion did not take effect.")
        assertNotNull(redisCache.get("c", TestData::class.java, null as String?), "clearRegion incorrectly cleared extra region.")
        assertNotNull(redisCache.get("d", TestData::class.java, null as String?), "clearRegion incorrectly cleared extra region.")
        assertNotNull(redisCache.get("e", TestData::class.java, ""), "clearRegion incorrectly cleared extra region.")
        assertNotNull(redisCache.get("f", TestData::class.java, "region3"), "clearRegion incorrectly cleared extra region.")
    }

    /**
     * Method: clear()
     */
    @Test
    @Throws(Exception::class)
    fun testClear() {
        val `val` = TestData()
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
        val list = listOf(TestData(), TestData(), TestData())

        redisCache.set("list-test", list, 5000L)

        val tr = object : TypeReference<List<TestData>>() {}
        val value = redisCache.get("list-test", tr.type)
        assertNotNull(value)

        val listData = value as List<*>
        assertEquals(3, listData.size)

        assertEquals(list, listData)
    }

    @Test
    @Throws(Exception::class)
    fun testMap() {
        val map = mapOf(
            "123" to TestData(),
            "234" to TestData(),
            "345" to TestData()
        )

        redisCache.set("list-test", map, 5000L)

        val tr = object : TypeReference<Map<String, TestData>>() {}
        val value = redisCache.get("list-test", tr.type)
        assertNotNull(value)

        val listData = value as Map<*, *>
        assertEquals(3, listData.size)

        assertEquals(map.toList(), listData.toList())
    }

    @Test
    @Throws(Exception::class)
    fun testMapGetOrSet() {
        val map: Map<String, TestData> = mapOf(
            "123" to TestData(),
            "234" to TestData(),
            "345" to TestData()
        )

        val map2: Map<String, TestData> = mapOf(
            "123" to TestData(),
            "234" to TestData(),
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
        val value = redisCache.get("list-test", tr.type)
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
            "${keyPrefix}-1" to TestData(),
            "${keyPrefix}-2" to TestData(),
            "${keyPrefix}-3" to TestData()
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
            val cached = redisCache.get(key, TestData::class.java, region)
            assertEquals(value, cached, "Cached value for key '$key' does not match the original.")
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
    fun testRemoveMulti() {
        val keyPrefix = "multi-key-test"
        val region = "region1"

        // Prepare multiple keys and values
        val testValues = mapOf(
            "${keyPrefix}-1" to TestData(),
            "${keyPrefix}-2" to TestData(),
            "${keyPrefix}-3" to TestData()
        )

        // Save all key-values with expiration and absolute time policy
        redisCache.setMulti(
            keyAndValues = testValues,
            expireMills = 600 * 1000,
            timePolicy = TimePolicy.Absolute,
            region = region
        )

        for ((key, value) in testValues) {
            val cached = redisCache.get(key, TestData::class.java, region)
            assertEquals(value, cached, "Cached value for key '$key' does not match the original.")
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

    private data class TestData(
        var intValue: Int = Random.nextInt(),
        var stringValue: String = UUID.randomUUID().toString()
    )
}
