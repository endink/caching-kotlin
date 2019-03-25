package com.labijie.caching.testing.configuration

import com.labijie.caching.testing.bean.ISimpleInterface
import com.labijie.caching.testing.bean.SimpleInterfaceImpl
import com.labijie.caching.testing.bean.SimpleScopedBean
import com.labijie.caching.testing.bean.SimpleTestingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-25
 */
@Configuration
@EnableAspectJAutoProxy
class TestConfiguration {

    @Bean
    fun simpleTestingBean(): SimpleTestingBean {
        return SimpleTestingBean()
    }

    @Bean
    fun simpleInterfaceImpl(): ISimpleInterface {
        return SimpleInterfaceImpl()
    }

    @Bean
    fun simpleScopedBean(simple:SimpleTestingBean): SimpleScopedBean {
        return SimpleScopedBean(simple)
    }
}