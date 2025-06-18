package com.labijie.caching.configuration

import com.labijie.caching.aot.TestRuntimeHintsRegistrar
import com.labijie.caching.bean.ISimpleInterface
import com.labijie.caching.bean.SimpleInterfaceImpl
import com.labijie.caching.bean.SimpleScopedBean
import com.labijie.caching.bean.SimpleTestingBean
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.ImportRuntimeHints

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-25
 */
@Configuration(proxyBeanMethods = false)
@EnableAspectJAutoProxy
@ImportRuntimeHints(TestRuntimeHintsRegistrar::class)
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
    fun simpleScopedBean(simple: SimpleTestingBean): SimpleScopedBean {
        return SimpleScopedBean(simple)
    }
}