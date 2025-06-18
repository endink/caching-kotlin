package com.labijie.caching

import com.labijie.caching.bean.ISimpleInterface
import com.labijie.caching.configuration.CachingAutoConfiguration
import com.labijie.caching.configuration.TestConfiguration
import com.labijie.caching.model.ArgumentObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.aot.DisabledInAotMode
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.BeforeTest

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-25
 */
@ExtendWith(SpringExtension::class)
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
    fun cacheMethod(){
        val args = ArgumentObject()
        val r = simple.getCached(arg =  args)

        val cached = this.cacheManager.get(args.stringValue, ArgumentObject::class)
        // it will be no cache because interface annotation
        Assertions.assertNull(cached)
    }
}