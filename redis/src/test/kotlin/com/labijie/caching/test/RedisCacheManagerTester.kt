package com.labijie.caching.test

import com.labijie.caching.ICacheManager
import com.labijie.caching.redis.RedisCacheManager
import com.labijie.caching.redis.configuration.RedisCacheConfig
import com.labijie.caching.redis.configuration.RedisRegionOptions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.random.Random

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
class RedisCacheManagerTester {
    private lateinit var redisCache: ICacheManager

    @BeforeEach
    @Throws(Exception::class)
    fun before() {
        this.redisCache = this.createCache()
    }

    @AfterEach
    @Throws(Exception::class)
    fun after() {
        this.redisCache.clear()
    }

    protected fun createCache(): RedisCacheManager {
        val redisConfig = RedisCacheConfig()
        redisConfig.regions.add(RedisRegionOptions("default", uri = "${TestingServer.serverUri}/0"))
        redisConfig.regions.add(RedisRegionOptions("region1",uri = "${TestingServer.serverUri}/1"))
        redisConfig.regions.add(RedisRegionOptions("region2",uri = "${TestingServer.serverUri}/2"))
        redisConfig.regions.add(RedisRegionOptions("region3",uri = "${TestingServer.serverUri}/3"))

        return RedisCacheManager(redisConfig)
    }

    /**
     * Method: get(String key, String region)
     */
    @Test
    @Throws(Exception::class)
    fun testGet() {

        Assertions.assertNull(redisCache.get("a", "region1"), "当值不存在时 get 应为 null")
        Assertions.assertNull(redisCache.get("b", "region2"), "当值不存在时 get 应为 null")
        Assertions.assertNull(redisCache.get("a", ""), "当值不存在时 get 应为 null")
        Assertions.assertNull(redisCache.get("a", null), "当值不存在时 get 应为 null")

        val `val` = TestData()
        redisCache.set("a", `val`, null, false, "region2")
        val saved = redisCache.get("a", "region2")
        Assertions.assertEquals(`val`, saved, "get 方法取到的值和 set 放入的值不一致。")
    }

    /**
     * Method: set(String key, Object data, Long timeoutMilliseconds, String region, boolean useSlidingExpiration)
     */
    @Test
    @Throws(Exception::class)
    fun testSet() {
        val `val` = TestData()
        redisCache.set("a", `val`, null, false, "region1")
        redisCache.set("b", `val`, 5000L, false, "region2")
        redisCache.set("c", `val`, 5000L, true, null)
        redisCache.set("d", `val`, null, true, null)
        redisCache.set("e", `val`, null, true, "")

        Assertions.assertEquals(`val`, redisCache.get("a", "region1"), "get 方法取到的值和 set 放入的值不一致。")
        Assertions.assertEquals(`val`, redisCache.get("b", "region2"), "get 方法取到的值和 set 放入的值不一致。")
        Assertions.assertEquals(`val`, redisCache.get("c", null as String?), "get 方法取到的值和 set 放入的值不一致。")
        Assertions.assertEquals(`val`, redisCache.get("d", null as String?), "get 方法取到的值和 set 放入的值不一致。")
        Assertions.assertEquals(`val`, redisCache.get("e", ""), "get 方法取到的值和 set 放入的值不一致。")
    }

    /**
     * Method: remove(String key, String region)
     */
    @Test
    @Throws(Exception::class)
    fun testRemove() {
        val `val` = TestData()
        redisCache.set("a", `val`, null, false, "region1")
        redisCache.set("b", `val`, 5000L, false, "region2")
        redisCache.set("c", `val`, 5000L, true, null)
        redisCache.set("d", `val`, null, true, null)
        redisCache.set("e", `val`, null, true, "")

        redisCache.remove("a", "region1")
        redisCache.remove("b", "region2")
        redisCache.remove("c", null)
        redisCache.remove("d", null)
        redisCache.remove("e", "")

        Assertions.assertNull(redisCache.get("a", "region1"), "remove 方法未生效。")
        Assertions.assertNull(redisCache.get("b", "region2"), "remove 方法未生效。")
        Assertions.assertNull(redisCache.get("c", null as String?), "remove 方法未生效。")
        Assertions.assertNull(redisCache.get("d", null as String?), "remove 方法未生效。")
        Assertions.assertNull(redisCache.get("e", ""), "remove 方法未生效。")
    }


    /**
     * Method: clearRegion(String region)
     */
    @Test
    @Throws(Exception::class)
    fun testClearRegion() {
        val `val` = TestData()
        redisCache.set("a", `val`, null, false, "region1")
        redisCache.set("b", `val`, 5000L, false, "region2")
        redisCache.set("c", `val`, 5000L, true)
        redisCache.set("d", `val`, null, true)
        redisCache.set("e", `val`, null, true, "")
        redisCache.set("f", `val`, 5000L, false, "region3")

        redisCache.clearRegion("region1")
        redisCache.clearRegion("region2")

        Assertions.assertNull(redisCache.get("a", "region1"), "clearRegion 方法未生效。")
        Assertions.assertNull(redisCache.get("b", "region2"), "clearRegion 方法未生效。")
        Assertions.assertNotNull(redisCache.get("c", null as String?), "clearRegion 清除了多余的区域。")
        Assertions.assertNotNull(redisCache.get("d", null as String?), "clearRegion 清除了多余的区域。")
        Assertions.assertNotNull(redisCache.get("e", ""), "clearRegion 清除了多余的区域。")
        Assertions.assertNotNull(redisCache.get("f", "region3"), "clearRegion 清除了多余的区域。")
    }

    /**
     * Method: clear()
     */
    @Test
    @Throws(Exception::class)
    fun testClear() {
        val `val` = TestData()
        redisCache.set("a", `val`, null, false, "region1")
        redisCache.set("b", `val`, 5000L, false, "region2")
        redisCache.set("c", `val`, 5000L, true)
        redisCache.set("d", `val`, null, true)
        redisCache.set("e", `val`, null, true, "")

        redisCache.clear()

        Assertions.assertNull(redisCache.get("a", "region1"), "clear 方法未生效。")
        Assertions.assertNull(redisCache.get("b", "region2"), "clear 方法未生效。")
        Assertions.assertNull(redisCache.get("c", null as String?), "clear 方法未生效。")
        Assertions.assertNull(redisCache.get("d", null as String?), "clear 方法未生效。")
        Assertions.assertNull(redisCache.get("e", ""), "clear 方法未生效。")
    }

    private data class TestData(
        var intValue:Int = Random.nextInt(),
        var stringValue:String = UUID.randomUUID().toString()
    )
}