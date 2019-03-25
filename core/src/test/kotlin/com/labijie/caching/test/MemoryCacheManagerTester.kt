package com.labijie.caching.test

import com.labijie.caching.ICacheManager
import com.labijie.caching.TimePolicy
import com.labijie.caching.memory.MemoryCacheManager
import org.junit.Assert
import org.junit.Test
import java.lang.reflect.InvocationTargetException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-02
 */
class MemoryCacheManagerTester {
    private lateinit var memoryCache: ICacheManager

    @BeforeTest
    @Throws(Exception::class)
    fun before() {
        this.memoryCache = this.createCache()
    }

    @AfterTest
    @Throws(Exception::class)
    fun after() {
        this.memoryCache.clear()
    }

    protected fun createCache() = MemoryCacheManager()

    /**
     * Method: get(String key, String region)
     */
    @Test
    @Throws(Exception::class)
    fun testGet() {

        Assert.assertNull("当值不存在时 get 应为 null", memoryCache.get("a", "b"))
        Assert.assertNull("当值不存在时 get 应为 null", memoryCache.get("b", "a"))
        Assert.assertNull("当值不存在时 get 应为 null", memoryCache.get("a", ""))
        Assert.assertNull("当值不存在时 get 应为 null", memoryCache.get("a", null as String?))

        val `val` = Any()
        memoryCache.set("a", `val`, null, TimePolicy.Absolute, "b")
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, memoryCache.get("a", "b"))
    }

    /**
     * Method: set(String key, Object data, Long timeoutMilliseconds, String region, boolean useSlidingExpiration)
     */
    @Test
    @Throws(Exception::class)
    fun testSet() {
        val `val` = Any()
        memoryCache.set("a", `val`, null, TimePolicy.Absolute, "region1")
        memoryCache.set("b", `val`, 5000L, TimePolicy.Absolute, "region2")
        memoryCache.set("c", `val`, 5000L, TimePolicy.Sliding, null)
        memoryCache.set("d", `val`, null, TimePolicy.Sliding, null)
        memoryCache.set("e", `val`, null, TimePolicy.Sliding, "")

        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, memoryCache.get("a", "region1"))
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, memoryCache.get("b", "region2"))
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, memoryCache.get("c", null as String?))
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, memoryCache.get("d", null as String?))
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, memoryCache.get("e", ""))
    }

    /**
     * Method: remove(String key, String region)
     */
    @Test
    @Throws(Exception::class)
    fun testRemove() {
        val `val` = Any()
        memoryCache.set("a", `val`, null, TimePolicy.Absolute, "region1")
        memoryCache.set("b", `val`, 5000L, TimePolicy.Absolute, "region2")
        memoryCache.set("c", `val`, 5000L, TimePolicy.Sliding, null)
        memoryCache.set("d", `val`, null, TimePolicy.Sliding, null)
        memoryCache.set("e", `val`, null, TimePolicy.Sliding, "")

        memoryCache.remove("a", "region1")
        memoryCache.remove("b", "region2")
        memoryCache.remove("c", null)
        memoryCache.remove("d", null)
        memoryCache.remove("e", "")

        Assert.assertNull("remove 方法未生效。", memoryCache.get("a", "region1"))
        Assert.assertNull("remove 方法未生效。", memoryCache.get("b", "region2"))
        Assert.assertNull("remove 方法未生效。", memoryCache.get("c", null as String?))
        Assert.assertNull("remove 方法未生效。", memoryCache.get("d", null as String?))
        Assert.assertNull("remove 方法未生效。", memoryCache.get("e", ""))
    }


    /**
     * Method: clearRegion(String region)
     */
    @Test
    @Throws(Exception::class)
    fun testClearRegion() {
        val `val` = Any()
        memoryCache.set("a", `val`, null, TimePolicy.Absolute, "region1")
        memoryCache.set("b", `val`, 5000L, TimePolicy.Absolute, "region2")
        memoryCache.set("c", `val`, 5000L, TimePolicy.Sliding)
        memoryCache.set("d", `val`, null, TimePolicy.Sliding)
        memoryCache.set("e", `val`, null, TimePolicy.Sliding, "")
        memoryCache.set("f", `val`, 5000L, TimePolicy.Absolute, "region3")

        memoryCache.clearRegion("region1")
        memoryCache.clearRegion("region2")

        Assert.assertNull("clearRegion 方法未生效。", memoryCache.get("a", "region1"))
        Assert.assertNull("clearRegion 方法未生效。", memoryCache.get("b", "region2"))
        Assert.assertNotNull("clearRegion 清除了多余的区域。", memoryCache.get("c", null as String?))
        Assert.assertNotNull("clearRegion 清除了多余的区域。", memoryCache.get("d", null as String?))
        Assert.assertNotNull("clearRegion 清除了多余的区域。", memoryCache.get("e", ""))
        Assert.assertNotNull("clearRegion 清除了多余的区域。", memoryCache.get("f", "region3"))
    }

    /**
     * Method: clear()
     */
    @Test
    @Throws(Exception::class)
    fun testClear() {
        val `val` = Any()
        memoryCache.set("a", `val`, null, TimePolicy.Absolute, "region1")
        memoryCache.set("b", `val`, 5000L, TimePolicy.Absolute, "region2")
        memoryCache.set("c", `val`, 5000L, TimePolicy.Sliding)
        memoryCache.set("d", `val`, null, TimePolicy.Sliding)
        memoryCache.set("e", `val`, null, TimePolicy.Sliding, "")

        memoryCache.clear()

        Assert.assertNull("clear 方法未生效。", memoryCache.get("a", "region1"))
        Assert.assertNull("clear 方法未生效。", memoryCache.get("b", "region2"))
        Assert.assertNull("clear 方法未生效。", memoryCache.get("c", null as String?))
        Assert.assertNull("clear 方法未生效。", memoryCache.get("d", null as String?))
        Assert.assertNull("clear 方法未生效。", memoryCache.get("e", ""))
    }

    /**
     * Method: getRegionNameFormFullKey(String fullKey)
     */
    @Test
    @Throws(Exception::class)
    fun testGetRegionNameFormFullKey() {

        try {
            val method =
                MemoryCacheManager::class.java.getDeclaredMethod("getRegionNameFormFullKey", String::class.java)
            method.isAccessible = true
            val regionName = method.invoke(memoryCache, "a|b") as String
            Assert.assertEquals("a", regionName)

        } catch (e: NoSuchMethodException) {
        } catch (e: IllegalAccessException) {
        } catch (e: InvocationTargetException) {
        }

    }
}