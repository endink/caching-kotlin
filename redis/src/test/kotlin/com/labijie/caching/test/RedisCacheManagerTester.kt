package com.labijie.caching.test

import com.fasterxml.jackson.core.type.TypeReference
import com.labijie.caching.ICacheManager
import com.labijie.caching.TimePolicy
import com.labijie.caching.getGenericType
import com.labijie.caching.getOrSet
import com.labijie.caching.redis.RedisCacheManager
import com.labijie.caching.redis.configuration.RedisCacheConfig
import com.labijie.caching.redis.configuration.RedisRegionOptions
import org.junit.jupiter.api.Assertions
import java.time.Duration
import java.util.*
import kotlin.test.Test
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
abstract class RedisCacheManagerTester {
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

//        val cacheManager:ICacheManager? = null
//        val rm = cacheManager as RedisCacheManager
//        val redisClient = rm.getClient("regionName")
//        val command = redisClient.connection.sync()
//        val oldValue = command.getset("key".toByteArray(Charsets.UTF_8), System.currentTimeMillis().toString())

        return RedisCacheManager(redisConfig)
    }

    protected abstract fun getSerializerName():String

    /**
     * Method: get(String key, String region)
     */
    @Test
    @Throws(Exception::class)
    fun testGet() {
        Assert.assertNull("当值不存在时 get 应为 null", redisCache.get("a", String::class.java,"b"))
        Assert.assertNull( "当值不存在时 get 应为 null", redisCache.get("b", String::class.java,"a"))
        Assert.assertNull("当值不存在时 get 应为 null",redisCache.get("a", String::class.java,""))
        Assert.assertNull("当值不存在时 get 应为 null", redisCache.get("a", String::class.java,null as String?))

        val `val` = TestData()
        redisCache.set("a", `val`, null, TimePolicy.Absolute, "b")
        val vv = redisCache.get("a", TestData::class.java,"b")
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, vv)
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

        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, redisCache.get("a", TestData::class.java,"region1"))
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, redisCache.get("b", TestData::class.java,"region2"))
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, redisCache.get("c", TestData::class.java,null as String?))
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, redisCache.get("d", TestData::class.java,null as String?))
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, redisCache.get("e", TestData::class.java,""))
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

        Assert.assertNull("remove 方法未生效。", redisCache.get("a", TestData::class.java,"region1"))
        Assert.assertNull("remove 方法未生效。", redisCache.get("b", TestData::class.java,"region2"))
        Assert.assertNull("remove 方法未生效。", redisCache.get("c", TestData::class.java,null as String?))
        Assert.assertNull("remove 方法未生效。", redisCache.get("d", TestData::class.java,null as String?))
        Assert.assertNull("remove 方法未生效。", redisCache.get("e", TestData::class.java,""))
    }

    @Test
    fun testGeneric(){
        val lst = listOf(TestData())
        redisCache.set("b", lst, 5000L, TimePolicy.Absolute, "region2")
        redisCache.get("b", getGenericType(List::class.java,TestData::class.java))
        println(lst)
        Assertions.assertTrue(lst.isNotEmpty())
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

        Assert.assertNull("clearRegion 方法未生效。", redisCache.get("a", TestData::class.java,"region1"))
        Assert.assertNull("clearRegion 方法未生效。", redisCache.get("b", TestData::class.java,"region2"))
        Assert.assertNotNull("clearRegion 清除了多余的区域。", redisCache.get("c", TestData::class.java,null as String?))
        Assert.assertNotNull("clearRegion 清除了多余的区域。", redisCache.get("d", TestData::class.java,null as String?))
        Assert.assertNotNull("clearRegion 清除了多余的区域。", redisCache.get("e", TestData::class.java,""))
        Assert.assertNotNull("clearRegion 清除了多余的区域。", redisCache.get("f", TestData::class.java,"region3"))
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

        Assert.assertNull("clear 方法未生效。", redisCache.get("a", TestData::class.java,"region1"))
        Assert.assertNull("clear 方法未生效。", redisCache.get("b", TestData::class.java,"region2"))
        Assert.assertNull("clear 方法未生效。", redisCache.get("c", TestData::class.java,null as String?))
        Assert.assertNull("clear 方法未生效。", redisCache.get("d", TestData::class.java,null as String?))
        Assert.assertNull("clear 方法未生效。", redisCache.get("e", TestData::class.java,""))
    }

    @Test
    @Throws(Exception::class)
    fun testList(){
        val list = listOf(TestData(), TestData(), TestData())

        redisCache.set("list-test", list, 5000L)

        val tr = object: TypeReference<List<TestData>>(){}
        val value = redisCache.get("list-test", tr.type)
        Assertions.assertNotNull(value)

        val listData = value as List<*>
        Assertions.assertEquals(3, listData.size)

        Assertions.assertArrayEquals(list.toTypedArray(), listData.toTypedArray())
    }

    @Test
    @Throws(Exception::class)
    fun testMap(){
        val map = mapOf(
            "123" to TestData(),
            "234" to TestData(),
            "345" to TestData())

        redisCache.set("list-test", map, 5000L)

        val tr = object: TypeReference<Map<String, TestData>>(){}
        val value = redisCache.get("list-test", tr.type)
        Assertions.assertNotNull(value)

        val listData = value as Map<*, *>
        Assertions.assertEquals(3, listData.size)

        Assertions.assertArrayEquals(map.toList().toTypedArray(), listData.toList().toTypedArray())
    }

    @Test
    @Throws(Exception::class)
    fun testMapGetOrSet(){
        val map:Map<String, TestData> = mapOf(
            "123" to TestData(),
            "234" to TestData(),
            "345" to TestData())

        val map2:Map<String, TestData> = mapOf(
            "123" to TestData(),
            "234" to TestData(),
            "345" to TestData())

        val data = redisCache.getOrSet("list-test", Duration.ofSeconds(60)){ map }

        val data2 = redisCache.getOrSet("list-test", Duration.ofSeconds(60)){
            map2
        }

        Assertions.assertTrue(map === data)

        Assertions.assertNotNull(data)
        Assertions.assertNotNull(data2)
        Assertions.assertArrayEquals(data!!.toList().toTypedArray(), data2!!.toList().toTypedArray())

        val tr = object: TypeReference<Map<String, TestData>>(){}
        val value = redisCache.get("list-test", tr.type)
        Assertions.assertNotNull(value)

        val listData = value as Map<*, *>
        Assertions.assertEquals(3, listData.size)

        Assertions.assertArrayEquals(map.toList().toTypedArray(), listData.toList().toTypedArray())
    }

    private data class TestData(
        var intValue:Int = Random.nextInt(),
        var stringValue:String = UUID.randomUUID().toString()
    )

}