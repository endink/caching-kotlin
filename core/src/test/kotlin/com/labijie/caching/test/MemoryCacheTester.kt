package com.labijie.caching.test

import com.labijie.caching.CacheException
import com.labijie.caching.IChangeToken
import com.labijie.caching.memory.CacheEntry
import com.labijie.caching.memory.MemoryCache
import com.labijie.caching.memory.MemoryCacheEntryOptions
import com.labijie.caching.memory.MemoryCacheOptions
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.Closeable
import java.lang.IllegalArgumentException
import java.lang.reflect.InvocationTargetException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.function.Consumer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-02
 */
class MemoryCacheTester {
    private lateinit var memoryCache: MemoryCache

    @BeforeTest
    @Throws(Exception::class)
    fun before() {
        memoryCache = MemoryCache(MemoryCacheOptions())
    }

    @AfterTest
    @Throws(Exception::class)
    fun after() {
        memoryCache.close()
    }


    /**
     * Method: size()
     */
    @Test
    @Throws(Exception::class)
    fun testSize() {
        Assert.assertEquals("创建 memoryCache 新实例后数量不正确。",0, memoryCache.size)

        val dt = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(2)

        memoryCache.set("cc", Any(), dt)
        Assert.assertEquals("添加第 1 个元素后数量不正确。",1, memoryCache.size)

        memoryCache.set("dd", Any(), dt)
        Assert.assertEquals("添加第 2 个元素后数量不正确。",2, memoryCache.size)

        memoryCache.remove("cc")
        Assert.assertEquals( "移除第 1 个元素后数量不正确。",1, memoryCache.size)

        memoryCache.remove("dd")
        Assert.assertEquals("移除第 2 个元素后数量不正确。",0, memoryCache.size)
    }

    /**
     * Method: createEntry(Object key)
     */
    @Test
    @Throws(Exception::class)
    fun testCreateEntry() {
        val entry = memoryCache.createEntry("cc")
        Assert.assertNotNull("创建 createEntry 返回了空值。", entry)
    }

    /**
     * Method: getOrDefault(Object key, Object defaultValue)
     */
    @Test
    @Throws(Exception::class)
    fun testGetOrDefault() {
        val value = memoryCache.getOrDefault("1", null)
        Assert.assertNull("不存在的键调用 getOrDefault 应该返回 null。", value)

        memoryCache.set("2", Any(), 3000L)
        val value2 = memoryCache.getOrDefault("2", null)
        Assert.assertNotNull("存在的键调用 getOrDefault 返回了 null。", value2)

        val newObj = Any()
        val value3 = memoryCache.getOrDefault("3", newObj)
        Assert.assertTrue("getOrDefault 新建 key 时返回了不同的引用对象。", value3 === newObj)
    }

    /**
     * Method: remove(Object key)
     */
    @Test
    @Throws(Exception::class)
    fun testRemove() {
        memoryCache.set("a", Any(), 3000L)
        val obj = memoryCache.get("a")
        Assert.assertNotNull("set 后再取值返回了 null。", obj)

        memoryCache.remove("a")
        val obj2 = memoryCache.get("a")
        Assert.assertNull("删除 key 后存在。", obj2)
    }

    /**
     * Method: set(Object key, T value, MemoryCacheEntryOptions options)
     */
    @Test
    @Throws(Exception::class)
    fun testSetForKeyValueOptions() {
        val options = MemoryCacheEntryOptions()
        options.slidingExpirationMilliseconds = 500L

        val value = Any()
        memoryCache.set("a", value, options)
        Thread.sleep(1000)
        val v = memoryCache.get("a")

        Assert.assertNull("set 测试滑动过期时间无效。", v)
    }

    /**
     * Method: set(Object key, T value, IChangeToken expirationToken)
     */
    @Test
    @Throws(Exception::class)
    fun testSetForKeyValueExpirationToken() {
        val value = Any()
        memoryCache.set("a", value, object : IChangeToken {
            override fun hasChanged(): Boolean {
                return true
            }

            override fun enableActiveChangeCallbacks(): Boolean {
                return true
            }

            override fun registerChangeCallback(callback: Consumer<Any>, state: Any): Closeable {
                return Closeable {
                    callback.accept(state)
                    println("callback invoked")
                }
            }
        })
        Thread.sleep(1000)
        val v = memoryCache.get("a")

        Assert.assertNull("set 测试滑动过期时间无效。", v)
    }

    /**
     * Method: set(Object key, T value, Date absoluteExpiration)
     */
    @Test
    @Throws(Exception::class)
    fun testSetForKeyValueAbsoluteExpiration() {
        val time = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1)
        val value = Any()
        memoryCache.set("a", value, time)
        var existed = memoryCache.get("a")
        Assert.assertNotNull("set 使用绝对过期时间缓存意外过期。", existed)
        Thread.sleep(2000)
        existed = memoryCache.get("a")
        Assert.assertNull("set 使用绝对过期时间缓存项未过期。", existed)
    }

    /**
     * Method: set(Object key, T value, Long slidingExpirationMilliseconds)
     */
    @Test
    @Throws(Exception::class)
    fun testSetForKeyValueSlidingExpirationMilliseconds() {
        val value = Any()
        memoryCache.set("a", value, 2000L)
        Thread.sleep(1000)
        var existed = memoryCache.get("a")
        Assert.assertTrue("set 使用滑动过期时间缓存项失效。", value === existed)

        Thread.sleep(1000)
        existed = memoryCache.get("a")
        Assert.assertTrue("set 使用滑动过期时间缓存项失效。", value === existed)

        Thread.sleep(1000)
        existed = memoryCache.get("a")
        Assert.assertTrue("set 使用滑动过期时间缓存项失效。", value === existed)

        Thread.sleep(3000)
        existed = memoryCache.get("a")
        Assert.assertNull("set 使用滑动过期时间缓存项未失效。", existed)
    }

    /**
     * Method: compact(double percentage)
     */
    @Test
    @Throws(Exception::class)
    fun testCompact() {
        memoryCache.set("1", Any(), 10 * 60 * 1000L)
        memoryCache.set("2", Any(), 10 * 60 * 1000L)
        memoryCache.set("3", Any(), 10 * 60 * 1000L)
        memoryCache.set("4", Any(), 10 * 60 * 1000L)
        memoryCache.set("5", Any(), 10 * 60 * 1000L)
        memoryCache.set("6", Any(), 10 * 60 * 1000L)
        memoryCache.set("7", Any(), 10 * 60 * 1000L)
        memoryCache.set("8", Any(), 10 * 60 * 1000L)
        memoryCache.set("9", Any(), 10 * 60 * 1000L)
        memoryCache.set("10", Any(), 10 * 60 * 1000L)

        memoryCache.compact(0.3)

        Assert.assertEquals("compact 收缩内存未生效。", 7, memoryCache.size)
    }


    /**
     * Method: checkClosed()
     */
    fun testCheckClosed() {
        Assertions.assertThrows(CacheException::class.java) {
            memoryCache.close()
            memoryCache.set("a", Any(), 1000L)
        }
    }

    /**
     * Method: setEntry(CacheEntry entry)
     */
    @Test
    @Throws(Exception::class)
    fun testSetEntry() {
        val entry = memoryCache.createEntry("33")
        Assert.assertEquals("createEntry 不应该自动添加的缓存项。", 0, memoryCache.size)
        try {
            val method = MemoryCache::class.java.getDeclaredMethod("setEntry", CacheEntry::class.java)
            method.isAccessible = true
            method.invoke(memoryCache, entry)

            Assert.assertEquals("setEntry(CacheEntry entry)  缓存项未被添加到列表。", 1, memoryCache.size)

        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

    }


    /**
     * Method: scanForExpiredItems(MemoryCache cache)
     */
    @Test
    @Throws(Exception::class)
    fun testScanForExpiredItems() {

        try {
            val method = MemoryCache::class.java.getDeclaredMethod("scanForExpiredItems", MemoryCache::class.java)
            method.isAccessible = true

            add10ItemToCacheManager()
            Thread.sleep(2001L)
            method.invoke(null, memoryCache)
            Assert.assertEquals("scanForExpiredItems 未把缓存项过期。", 8, memoryCache.size)


            memoryCache = MemoryCache(MemoryCacheOptions())
            add10ItemToCacheManager()
            Thread.sleep(5001L)
            method.invoke(null, memoryCache)
            Assert.assertEquals("scanForExpiredItems 未把缓存项过期。", 5, memoryCache.size)

            memoryCache = MemoryCache(MemoryCacheOptions())
            add10ItemToCacheManager()
            Thread.sleep(9001L)
            method.invoke(null, memoryCache)
            Assert.assertEquals("scanForExpiredItems 未把缓存项过期。", 1, memoryCache.size)


        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

    }

    private fun add10ItemToCacheManager() {
        memoryCache.set("1", Any(), 1000L)
        memoryCache.set("2", Any(), 2000L)
        memoryCache.set("3", Any(), 3000L)
        memoryCache.set("4", Any(), 4000L)
        memoryCache.set("5", Any(), 5000L)
        memoryCache.set("6", Any(), 6000L)
        memoryCache.set("7", Any(), 7000L)
        memoryCache.set("8", Any(), 8000L)
        memoryCache.set("9", Any(), 9000L)
        memoryCache.set("10", Any(), 10000L)
    }


}