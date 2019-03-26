package com.labijie.caching.test

import com.labijie.caching.ICacheManager
import com.labijie.caching.TimePolicy
import com.labijie.caching.memory.MemoryCacheManager
import com.labijie.caching.redis.RedisCacheManager
import com.labijie.caching.redis.configuration.RedisCacheConfig
import com.labijie.caching.redis.configuration.RedisRegionOptions
import org.junit.Assert
import org.junit.Test
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
class RedisCacheManagerTester {
    private lateinit var redisCache: ICacheManager

    @BeforeTest
    @Throws(Exception::class)
    fun before() {
        this.redisCache = this.createCache()
    }

    @AfterTest
    @Throws(Exception::class)
    fun after() {
        this.redisCache.clear()
    }

    protected fun createCache(): RedisCacheManager {
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

        return RedisCacheManager(redisConfig)
    }

    /**
     * Method: get(String key, String region)
     */
    @Test
    @Throws(Exception::class)
    fun testGet() {

        Assert.assertNull("当值不存在时 get 应为 null", redisCache.get("a", "b"))
        Assert.assertNull( "当值不存在时 get 应为 null", redisCache.get("b", "a"))
        Assert.assertNull("当值不存在时 get 应为 null",redisCache.get("a", ""))
        Assert.assertNull("当值不存在时 get 应为 null", redisCache.get("a", null as String?))

        val `val` = TestData()
        redisCache.set("a", `val`, null, TimePolicy.Absolute, "b")
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, redisCache.get("a", "b"))
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

        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, redisCache.get("a", "region1"))
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, redisCache.get("b", "region2"))
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, redisCache.get("c", null as String?))
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, redisCache.get("d", null as String?))
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, redisCache.get("e", ""))
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

        Assert.assertNull("remove 方法未生效。", redisCache.get("a", "region1"))
        Assert.assertNull("remove 方法未生效。", redisCache.get("b", "region2"))
        Assert.assertNull("remove 方法未生效。", redisCache.get("c", null as String?))
        Assert.assertNull("remove 方法未生效。", redisCache.get("d", null as String?))
        Assert.assertNull("remove 方法未生效。", redisCache.get("e", ""))
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

        Assert.assertNull("clearRegion 方法未生效。", redisCache.get("a", "region1"))
        Assert.assertNull("clearRegion 方法未生效。", redisCache.get("b", "region2"))
        Assert.assertNotNull("clearRegion 清除了多余的区域。", redisCache.get("c", null as String?))
        Assert.assertNotNull("clearRegion 清除了多余的区域。", redisCache.get("d", null as String?))
        Assert.assertNotNull("clearRegion 清除了多余的区域。", redisCache.get("e", ""))
        Assert.assertNotNull("clearRegion 清除了多余的区域。", redisCache.get("f", "region3"))
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

        Assert.assertNull("clear 方法未生效。", redisCache.get("a", "region1"))
        Assert.assertNull("clear 方法未生效。", redisCache.get("b", "region2"))
        Assert.assertNull("clear 方法未生效。", redisCache.get("c", null as String?))
        Assert.assertNull("clear 方法未生效。", redisCache.get("d", null as String?))
        Assert.assertNull("clear 方法未生效。", redisCache.get("e", ""))
    }

    private data class TestData(
        var intValue:Int = Random.nextInt(),
        var stringValue:String = UUID.randomUUID().toString()
    )
}