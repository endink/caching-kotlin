package com.labijie.caching

import com.labijie.caching.CacheOperation
import com.labijie.caching.ICacheManager
import com.labijie.caching.cacheScope
import com.labijie.caching.configuration.CachingAutoConfiguration
import com.labijie.caching.get
import com.labijie.caching.bean.SimpleScopedBean
import com.labijie.caching.bean.SimpleTestingBean
import com.labijie.caching.configuration.TestConfiguration
import com.labijie.caching.model.ArgumentObject
import org.junit.Assert
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
@ContextConfiguration(classes = [CachingAutoConfiguration::class, TestConfiguration::class])
class AnnotationClassBeanTester {

    @Autowired
    private lateinit var simple: SimpleTestingBean

    @Autowired
    private lateinit var scopedBean: SimpleScopedBean


    @Autowired
    private lateinit var cacheManager: ICacheManager

    @BeforeTest
    fun init() {
        cacheManager.clear()
    }

    @Test
    fun cacheAnnotationTest() {
        val args = ArgumentObject()
        val r = simple.getCached(args)

        val cached = this.cacheManager.get(args.stringValue, ArgumentObject::class)
        Assert.assertEquals(cached, r)

        val r2 = simple.getCached(args)
        Assert.assertEquals(cached, r2)
    }

    @Test
    fun cacheRemoveAnnotationTest() {
        val args = ArgumentObject()
        val r = simple.getCached(args)

        val cached = this.cacheManager.get(args.stringValue, ArgumentObject::class)

        Assert.assertEquals(r, cached)

        simple.removeCacheDelay2s(args.stringValue)

        Thread.sleep(3000)
        val cached2 = this.cacheManager.get(args.stringValue, ArgumentObject::class)
        Assert.assertNull(cached2)

        val r2 = simple.getCached(args)
        simple.removeCacheDelay5s(args.stringValue)

        val cached3 = this.cacheManager.get(args.stringValue, ArgumentObject::class)
        Assert.assertNotNull(cached3)

        Thread.sleep(6000)
        val cached4 = this.cacheManager.get(args.stringValue, ArgumentObject::class)
        Assert.assertNull(cached4)
    }

    @Test
    fun listCacheTest(){
        val list = simple.getCachedList()

        val list2 = simple.getCachedList()

        Assert.assertTrue(list === list2) //引用相同

        simple.removeCachedList()

        val list3 = simple.getCachedList()
        Assert.assertTrue(list !== list3)

        simple.getCachedList() //清理
    }

    @Test
    fun cacheAnnotationTestWithOptionalArgs() {
        val args = ArgumentObject()
        val r = simple.getCachedOptionalArgs(arg = args)

        val cached = this.cacheManager.get(args.stringValue, ArgumentObject::class)

        Assert.assertEquals(r, cached)
    }

    @Test
    fun scopedBeanPreventGetTest() {
        val args = ArgumentObject()
        val r = scopedBean.getWithoutGet(args)

        val cached = this.cacheManager.get(args.stringValue, ArgumentObject::class)
        Assert.assertNotNull(r)
        Assert.assertNull(cached)
    }

    @Test
    fun scopeMethodPreventGetTest() {
        cacheScope(CacheOperation.Get) {
            val cached = simple.getCached(ArgumentObject())
        }
    }

}