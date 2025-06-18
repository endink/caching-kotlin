import com.labijie.caching.component.ITransactionInjection
import com.labijie.caching.component.JdbcTransactionInjection
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author Anders Xiao
 * @date 2025-06-16
 */

@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(ITransactionInjection::class)
class JdbcCachingAutoConfiguration {

    @Bean
    @ConditionalOnClass(name = ["org.springframework.transaction.PlatformTransactionManager"])
    fun jdbcTransactionInjection(): JdbcTransactionInjection {
        return JdbcTransactionInjection()
    }
}