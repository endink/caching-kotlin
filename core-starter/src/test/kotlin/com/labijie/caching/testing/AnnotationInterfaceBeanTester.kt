package com.labijie.caching.testing

import com.labijie.caching.ICacheManager
import com.labijie.caching.configuration.CachingAutoConfiguration
import com.labijie.caching.testing.bean.ISimpleInterface
import com.labijie.caching.testing.bean.SimpleTestingBean
import com.labijie.caching.testing.configuration.TestConfiguration
import com.labijie.caching.testing.model.ArgumentObject
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
@ContextConfiguration(classes = [CachingAutoConfiguration::class, TestConfiguration::class])
class AnnotationInterfaceBeanTester {

    @Autowired
    private lateinit var simple : ISimpleInterface

    @Autowired
    private lateinit var cacheManager: ICacheManager

    @BeforeTest
    fun init(){
        cacheManager.clear()
    }

    @Test
    fun cacheAnnotationTestWithOptionalArgs(){
        val args = ArgumentObject()
        val r = simple.getCached(arg =  args)

        val cached = this.cacheManager.get(args.stringValue, ArgumentObject::class)
        // it will be no cache because interface annotation
        Assert.assertNull(cached)
    }
}