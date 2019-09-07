package com.labijie.caching.configuration

import com.labijie.caching.component.ITransactionInjection
import com.labijie.caching.component.JdbcTransactionInjection
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-26
 */
@Configuration
@ConditionalOnMissingBean(ITransactionInjection::class)
class JdbcCachingAutoConfiguration {

    @Bean
    @ConditionalOnClass(name = ["org.springframework.transaction.PlatformTransactionManager"])
    fun jdbcTransactionInjection(): JdbcTransactionInjection {
        return JdbcTransactionInjection()
    }
}


