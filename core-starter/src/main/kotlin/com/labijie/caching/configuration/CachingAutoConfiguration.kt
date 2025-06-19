package com.labijie.caching.configuration

import JdbcCachingAutoConfiguration
import com.labijie.caching.*
import com.labijie.caching.aspect.CacheGetAspect
import com.labijie.caching.aspect.CacheRemoveAspect
import com.labijie.caching.aspect.CacheScopeAspect
import com.labijie.caching.component.HashedWheelDelayTimer
import com.labijie.caching.component.IDelayTimer
import com.labijie.caching.component.ITransactionInjection
import com.labijie.caching.component.NoopTransactionInjection
import com.labijie.caching.memory.MemoryCacheManager
import com.labijie.caching.memory.MemoryCacheOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-22
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CachingProperties::class)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
class CachingAutoConfiguration {

    companion object {
        private val logger = LoggerFactory.getLogger(CachingAutoConfiguration::class.java)
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = ["infra.caching.provider"], havingValue = "none", matchIfMissing = false)
    protected class NoopCacheAutoConfiguration : InitializingBean {
        @Bean
        fun noopCacheManager(): NoopCacheManager {
            return NoopCacheManager.INSTANCE
        }

        override fun afterPropertiesSet() {
            logger.info("The cache has been disabled.")
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(JdbcCachingAutoConfiguration::class)
    protected class CachingAspectAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(ITransactionInjection::class)
        fun noopTransactionInjection(): NoopTransactionInjection {
            return NoopTransactionInjection()
        }


        @Bean
        @ConditionalOnMissingBean(ICacheScopeHolder::class)
        fun threadLocalCacheScopeHolder(): ThreadLocalCacheScopeHolder {
            return ThreadLocalCacheScopeHolder()
        }

        @Bean
        @ConditionalOnMissingBean(IDelayTimer::class)
        fun hashedWheelDelayTimer(): HashedWheelDelayTimer {
            return HashedWheelDelayTimer()
        }


        @Bean
        fun cacheRunner(cacheScopeHolder: ICacheScopeHolder): CacheRunner {
            return CacheRunner(cacheScopeHolder)
        }

        @Bean
        fun cacheScopeAspect(cacheScopeHolder: ICacheScopeHolder): CacheScopeAspect =
            CacheScopeAspect(cacheScopeHolder)

        @Bean
        fun cacheGetAspect(cacheScopeHolder: ICacheScopeHolder, cacheManager: ICacheManager): CacheGetAspect =
            CacheGetAspect(cacheManager, cacheScopeHolder)

        @Bean
        fun cacheRemoveAspect(transactionInjection: ITransactionInjection, cacheManager: ICacheManager, cacheScopeHolder: ICacheScopeHolder, delayTimer: IDelayTimer) =
            CacheRemoveAspect(cacheManager, cacheScopeHolder, delayTimer, transactionInjection)

        @Configuration(proxyBeanMethods = false)
        @ConditionalOnMissingBean(ICacheManager::class)
        protected class MemoryCacheAutoConfiguration {

            @Bean
            @ConfigurationProperties("infra.caching.memory")
            fun memoryCacheOptions(): MemoryCacheOptions {
                return MemoryCacheOptions()
            }

            @Bean
            fun memoryCacheManager(options: MemoryCacheOptions, cacheScopeHolder: ICacheScopeHolder): ScopedCacheManager {
                val innerCacheManager = MemoryCacheManager(options)
                return ScopedCacheManager(innerCacheManager)
            }
        }
    }

}