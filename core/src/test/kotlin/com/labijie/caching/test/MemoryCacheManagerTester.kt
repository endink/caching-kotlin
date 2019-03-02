package com.labijie.caching.test

import com.labijie.caching.memory.MemoryCacheManager
import com.labijie.caching.memory.MemoryCacheOptions
import org.junit.jupiter.api.*
import java.lang.reflect.InvocationTargetException

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-02
 */
class MemoryCacheManagerTester {

    private lateinit var memoryCache: MemoryCacheManager

    @BeforeEach
    @Throws(Exception::class)
    fun before() {
        this.memoryCache = MemoryCacheManager(MemoryCacheOptions())
    }

    @AfterEach
    @Throws(Exception::class)
    fun after() {
        this.memoryCache.clear()
    }

    /**
     * Method: get(String key, String region)
     */
    @Test
    @Throws(Exception::class)
    fun testGet() {

        Assertions.assertNull(memoryCache.get("a", "b"), "当值不存在时 get 应为 null")
        Assertions.assertNull(memoryCache.get("b", "a"), "当值不存在时 get 应为 null")
        Assertions.assertNull(memoryCache.get("a", ""), "当值不存在时 get 应为 null")
        Assertions.assertNull(memoryCache.get("a", null as String?), "当值不存在时 get 应为 null")

        val `val` = Any()
        memoryCache.set("a", `val`, null, false, "b")
        Assertions.assertEquals(`val`, memoryCache.get("a", "b"),"get 方法取到的值和 set 放入的值不一致。")
    }

    /**
     * Method: set(String key, Object data, Long timeoutMilliseconds, String region, boolean useSlidingExpiration)
     */
    @Test
    @Throws(Exception::class)
    fun testSet() {
        val `val` = Any()
        memoryCache.set("a", `val`, null,  false, "region1")
        memoryCache.set("b", `val`, 5000L, false, "region2")
        memoryCache.set("c", `val`, 5000L, true, null)
        memoryCache.set("d", `val`, null,  true, null)
        memoryCache.set("e", `val`, null, true, "")

        Assertions.assertEquals(`val`, memoryCache.get("a", "region1"),"get 方法取到的值和 set 放入的值不一致。")
        Assertions.assertEquals(`val`, memoryCache.get("b", "region2"),"get 方法取到的值和 set 放入的值不一致。")
        Assertions.assertEquals(`val`, memoryCache.get("c", null as String?),"get 方法取到的值和 set 放入的值不一致。")
        Assertions.assertEquals(`val`, memoryCache.get("d", null as String?),"get 方法取到的值和 set 放入的值不一致。")
        Assertions.assertEquals(`val`, memoryCache.get("e", ""),"get 方法取到的值和 set 放入的值不一致。")
    }

    /**
     * Method: remove(String key, String region)
     */
    @Test
    @Throws(Exception::class)
    fun testRemove() {
        val `val` = Any()
        memoryCache.set("a", `val`, null,  false, "region1")
        memoryCache.set("b", `val`, 5000L, false,"region2")
        memoryCache.set("c", `val`, 5000L, true, null)
        memoryCache.set("d", `val`, null, true, null)
        memoryCache.set("e", `val`, null, true, "")

        memoryCache.remove("a", "region1")
        memoryCache.remove("b", "region2")
        memoryCache.remove("c", null)
        memoryCache.remove("d", null)
        memoryCache.remove("e", "")

        Assertions.assertNull(memoryCache.get("a", "region1"),"remove 方法未生效。")
        Assertions.assertNull(memoryCache.get("b", "region2"),"remove 方法未生效。")
        Assertions.assertNull(memoryCache.get("c", null as String?),"remove 方法未生效。")
        Assertions.assertNull(memoryCache.get("d", null as String?),"remove 方法未生效。")
        Assertions.assertNull(memoryCache.get("e", ""),"remove 方法未生效。")
    }


    /**
     * Method: clearRegion(String region)
     */
    @Test
    @Throws(Exception::class)
    fun testClearRegion() {
        val `val` = Any()
        memoryCache.set("a", `val`, null,  false, "region1")
        memoryCache.set("b", `val`, 5000L, false, "region2")
        memoryCache.set("c", `val`, 5000L,  true)
        memoryCache.set("d", `val`, null,  true)
        memoryCache.set("e", `val`, null, true, "")
        memoryCache.set("f", `val`, 5000L,  false, "region3")

        memoryCache.clearRegion("region1")
        memoryCache.clearRegion("region2")

        Assertions.assertNull(memoryCache.get("a", "region1"), "clearRegion 方法未生效。")
        Assertions.assertNull(memoryCache.get("b", "region2"), "clearRegion 方法未生效。")
        Assertions.assertNotNull(memoryCache.get("c", null as String?), "clearRegion 清除了多余的区域。")
        Assertions.assertNotNull(memoryCache.get("d", null as String?), "clearRegion 清除了多余的区域。")
        Assertions.assertNotNull(memoryCache.get("e", ""), "clearRegion 清除了多余的区域。")
        Assertions.assertNotNull(memoryCache.get("f", "region3"), "clearRegion 清除了多余的区域。")
    }

    /**
     * Method: clear()
     */
    @Test
    @Throws(Exception::class)
    fun testClear() {
        val `val` = Any()
        memoryCache.set("a", `val`, null,  false, "region1")
        memoryCache.set("b", `val`, 5000L,  false, "region2")
        memoryCache.set("c", `val`, 5000L,  true)
        memoryCache.set("d", `val`, null,  true)
        memoryCache.set("e", `val`, null,  true, "")

        memoryCache.clear()

        Assertions.assertNull(memoryCache.get("a", "region1"), "clear 方法未生效。")
        Assertions.assertNull(memoryCache.get("b", "region2"), "clear 方法未生效。")
        Assertions.assertNull(memoryCache.get("c", null as String?), "clear 方法未生效。")
        Assertions.assertNull(memoryCache.get("d", null as String?), "clear 方法未生效。")
        Assertions.assertNull(memoryCache.get("e", ""), "clear 方法未生效。")
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
            Assertions.assertEquals("a", regionName)

        } catch (e: NoSuchMethodException) {
        } catch (e: IllegalAccessException) {
        } catch (e: InvocationTargetException) {
        }

    }

}