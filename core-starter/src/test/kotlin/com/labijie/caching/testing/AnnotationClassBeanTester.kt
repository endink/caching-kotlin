package com.labijie.caching.testing

import com.labijie.caching.ICacheManager
import com.labijie.caching.configuration.CachingAutoConfiguration
import com.labijie.caching.expression.SpELContext
import com.labijie.caching.expression.SpELEvaluator
import com.labijie.caching.testing.bean.SimpleTestingBean
import com.labijie.caching.testing.configuration.TestConfiguration
import com.labijie.caching.testing.model.ArgumentObject
import com.labijie.caching.testing.model.MethodObject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import kotlin.reflect.jvm.javaMethod
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
class AnnotationClassBeanTester(){

    @Autowired
    private lateinit var simple :SimpleTestingBean

    @Autowired
    private lateinit var cacheManager:ICacheManager

    @BeforeTest
    fun init(){
        cacheManager.clear()
    }

    @Test
    fun cacheAnnotationTest(){
        val args = ArgumentObject()
        val r = simple.getCached(args)

        val cached = this.cacheManager.get(args.stringValue, ArgumentObject::class)
        Assert.assertEquals(cached, r)

        val r2 = simple.getCached(args)
        Assert.assertEquals(cached, r2)
    }

    @Test
    fun cacheRemoveAnnotationTest(){
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
    fun cacheAnnotationTestWithOptionalArgs(){
        val args = ArgumentObject()
        val r = simple.getCachedOptionalArgs(arg =  args)

        val cached = this.cacheManager.get(args.stringValue, ArgumentObject::class)

        Assert.assertEquals(r, cached)
    }

}