package com.labijie.caching.configuration

import com.labijie.caching.ICacheManager
import com.labijie.caching.ICacheScopeHolder
import com.labijie.caching.ThreadLocalCacheScopeHolder
import com.labijie.caching.aspect.CacheGetAspect
import com.labijie.caching.aspect.CacheRemoveAspect
import com.labijie.caching.aspect.CacheScopeAspect
import com.labijie.caching.component.*
import com.labijie.caching.memory.MemoryCacheManager
import com.labijie.caching.memory.MemoryCacheOptions
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.transaction.PlatformTransactionManager

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-22
 */
@Configuration
@Import(JdbcCachingAutoConfiguration::class)
class CachingAutoConfiguration {

    @Configuration
    @ConditionalOnMissingBean(ICacheManager::class)
    protected class MemoryCacheAutoConfiguration {

        @Bean
        @ConfigurationProperties("infra.caching.memory")
        fun memoryCacheOptions(): MemoryCacheOptions {
            return MemoryCacheOptions()
        }

        @Bean
        fun memoryCacheManager(options: MemoryCacheOptions): MemoryCacheManager {
            return MemoryCacheManager(options)
        }
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
    @ConditionalOnMissingBean(ITransactionInjection::class)
    fun noopTransactionInjection(): NoopTransactionInjection {
        return NoopTransactionInjection()
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
}