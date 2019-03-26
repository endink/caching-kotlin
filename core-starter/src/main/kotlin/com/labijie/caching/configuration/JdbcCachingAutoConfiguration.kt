package com.labijie.caching.configuration

import com.labijie.caching.component.ITransactionInjection
import com.labijie.caching.component.JdbcTransactionInjection
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-26
 */
@Configuration
@ConditionalOnClass(PlatformTransactionManager::class)
@ConditionalOnMissingBean(ITransactionInjection::class)
class JdbcCachingAutoConfiguration {

    @Bean
    fun jdbcTransactionInjection(): JdbcTransactionInjection {
        return JdbcTransactionInjection()
    }
}


