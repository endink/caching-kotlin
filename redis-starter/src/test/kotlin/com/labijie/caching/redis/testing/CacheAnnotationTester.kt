package com.labijie.caching.redis.testing

import com.fasterxml.jackson.core.type.TypeReference
import com.labijie.caching.ICacheManager
import com.labijie.caching.configuration.CachingAutoConfiguration
import com.labijie.caching.get
import com.labijie.caching.redis.get
import com.labijie.caching.redis.testing.bean.SimpleTestingBean
import com.labijie.caching.redis.testing.configuration.CacheManagerFactory
import com.labijie.caching.redis.testing.configuration.TestConfiguration
import com.labijie.caching.redis.testing.model.ArgumentObject
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.BeforeTest

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-25
 */

@RunWith(SpringRunner::class)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@DataJdbcTest
@ContextConfiguration(classes = [TestConfiguration::class, CachingAutoConfiguration::class])
abstract class CacheAnnotationTester {

    @Autowired
    protected lateinit var simple: SimpleTestingBean


    @Autowired
    private lateinit var cacheManager:ICacheManager

    protected abstract fun getSerializerName():String

    @BeforeTest
    fun beforeTest() {
        cacheManager.clear()
    }

    @Test
    fun simpleCacheTest(){
        val data = simple.getCachedSimple()

        val data2 = simple.getCachedSimple()

        Assert.assertEquals(data, data2)

        simple.removeCachedSimple()

        val cached = cacheManager.get(SimpleTestingBean.SIMPLE_CACHE_KEY, ArgumentObject::class)

        Assert.assertNull(cached)
    }

    @Test
    fun mapCacheTest(){
        val data = simple.getCachedMap()

        val data2 = simple.getCachedMap()

        Assert.assertEquals(data.size, data2.size)
        Assert.assertArrayEquals(data.toList().toTypedArray(), data2.toList().toTypedArray())

        simple.removeCachedMap()

        val cached = cacheManager.get(SimpleTestingBean.MAP_CACHE_KEY, object:TypeReference<Map<String, ArgumentObject>>() {})

        Assert.assertNull(cached)
    }

    @Test
    fun listCacheTest(){
        val list = simple.getCachedList()

        val list2 = simple.getCachedList()

        Assert.assertTrue(list !== list2) //引用不相同

        Assert.assertEquals(list.size, list2.size)

        Assert.assertArrayEquals(list.toTypedArray(), list2.toTypedArray())

        simple.removeCachedList()

        val cached = cacheManager.get(SimpleTestingBean.LIST_CACHE_KEY, List::class)

        Assert.assertNull(cached)
    }

    @Test
    fun arrayCacheTest(){
        val list = simple.getCachedArray()

        val list2 = simple.getCachedArray()

        Assert.assertTrue(list !== list2) //引用不相同

        Assert.assertEquals(list.size, list2.size)

        Assert.assertArrayEquals(list, list2)

        simple.removeCachedArray()

        val cached = cacheManager.get(SimpleTestingBean.LIST_CACHE_KEY, List::class)

        Assert.assertNull(cached)
    }

}