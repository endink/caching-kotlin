package com.labijie.caching.test

import com.labijie.caching.ICacheManager
import com.labijie.caching.TimePolicy
import com.labijie.caching.getGenericType
import com.labijie.caching.memory.MemoryCacheManager
import com.labijie.caching.set
import org.junit.jupiter.api.Assertions
import kotlin.test.*
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
        Assert.assertNull("当值不存在时 get 应为 null", memoryCache.get("a", Any::class.java,"b"))
        Assert.assertNull("当值不存在时 get 应为 null", memoryCache.get("b", Any::class.java,"a"))
        Assert.assertNull("当值不存在时 get 应为 null", memoryCache.get("a", Any::class.java,""))
        Assert.assertNull("当值不存在时 get 应为 null", memoryCache.get("a", Any::class.java, null as String?))

        val `val` = Any()
        memoryCache.set("a", `val`, null, TimePolicy.Absolute, "b")
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, memoryCache.get("a", Any::class.java,"b"))
    }

    @Test
    fun testGenericGet(){
        val t = listOf(1,2,3,4)
        memoryCache.set("t",t,10_000, TimePolicy.Absolute, "b")

        val t1  = memoryCache.get("t", getGenericType(List::class.java,Int::class.java),"b")
        assert(t1 is List<*>)
        val ttt = t1 as List<*>
        Assertions.assertNotNull(ttt)
        Assertions.assertTrue(t.minus(ttt).isEmpty())
    }

    @Test
    fun testCustomGenericGet(){
        val t = GenericStub(1)
        memoryCache.set("t",t,10_000, TimePolicy.Absolute, "b")

        val t1  = memoryCache.get("t", getGenericType(GenericStub::class.java,Int::class.java),"b")

        assert(t1 is GenericStub<*>)
        val ttt = t1 as GenericStub<*>
        Assertions.assertNotNull(ttt)
        Assertions.assertTrue(ttt.item == 1)
    }

    @Test
    fun testCustomListGenericGet(){
        val t = listOf(GenericStub(1))
        memoryCache.set("t",t,10_000, TimePolicy.Absolute, "b")

        val t1  = memoryCache.get("t", getGenericType(List::class.java, getGenericType(GenericStub::class.java,Int::class.java)),"b")

        assert(t1 is List<*>)
        val ttt = t1 as List<*>
        Assertions.assertNotNull(ttt)

        assert(ttt.isNotEmpty())

        assert(ttt[0] is GenericStub<*>)

        val item = ttt[0] as? GenericStub<*>
        val value = item?.item
        assert(value != null && value is Int && value  == 1)
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

        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, memoryCache.get("a",Any::class.java, "region1"))
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, memoryCache.get("b",Any::class.java, "region2"))
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, memoryCache.get("c",Any::class.java, null as String?))
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, memoryCache.get("d",Any::class.java, null as String?))
        Assert.assertEquals("get 方法取到的值和 set 放入的值不一致。", `val`, memoryCache.get("e",Any::class.java, ""))
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

        Assert.assertNull("remove 方法未生效。", memoryCache.get("a", Any::class.java,"region1"))
        Assert.assertNull("remove 方法未生效。", memoryCache.get("b", Any::class.java,"region2"))
        Assert.assertNull("remove 方法未生效。", memoryCache.get("c", Any::class.java,null as String?))
        Assert.assertNull("remove 方法未生效。", memoryCache.get("d", Any::class.java,null as String?))
        Assert.assertNull("remove 方法未生效。", memoryCache.get("e", Any::class.java,""))
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

        Assert.assertNull("clearRegion 方法未生效。", memoryCache.get("a", Long::class.java,"region1"))
        Assert.assertNull("clearRegion 方法未生效。", memoryCache.get("b", Long::class.java,"region2"))
        Assert.assertNotNull("clearRegion 清除了多余的区域。", memoryCache.get("c", Long::class.java,null as String?))
        Assert.assertNotNull("clearRegion 清除了多余的区域。", memoryCache.get("d", Long::class.java,null as String?))
        Assert.assertNotNull("clearRegion 清除了多余的区域。", memoryCache.get("e", Long::class.java,""))
        Assert.assertNotNull("clearRegion 清除了多余的区域。", memoryCache.get("f", Long::class.java,"region3"))
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

        Assert.assertNull("clear 方法未生效。", memoryCache.get("a", Any::class.java, "region1"))
        Assert.assertNull("clear 方法未生效。", memoryCache.get("b", Any::class.java, "region2"))
        Assert.assertNull("clear 方法未生效。", memoryCache.get("c", Any::class.java, null as String?))
        Assert.assertNull("clear 方法未生效。", memoryCache.get("d", Any::class.java, null as String?))
        Assert.assertNull("clear 方法未生效。", memoryCache.get("e", Any::class.java, ""))
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

        } catch (_: NoSuchMethodException) {
        } catch (_: IllegalAccessException) {
        } catch (_: InvocationTargetException) {
        }

    }
}