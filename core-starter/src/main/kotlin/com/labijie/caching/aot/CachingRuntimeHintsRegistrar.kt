/**
 * @author Anders Xiao
 * @date 2025-06-16
 */
package com.labijie.caching.aot

import com.labijie.caching.annotation.Cache
import com.labijie.caching.annotation.CacheRemove
import com.labijie.caching.annotation.SuppressCache
import com.labijie.caching.component.JdbcTransactionInjection
import com.labijie.caching.component.NoopTransactionInjection
import com.labijie.caching.configuration.CachingAutoConfiguration
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.aot.hint.TypeReference


class CachingRuntimeHintsRegistrar : RuntimeHintsRegistrar {
    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        hints.reflection().registerTypes(
            listOf(
                TypeReference.of(Cache::class.java),
                TypeReference.of(CacheRemove::class.java),
                TypeReference.of(SuppressCache::class.java)
            )
        ) {
            it.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS)
                .withMembers(MemberCategory.INVOKE_DECLARED_METHODS)
                .withMembers(MemberCategory.DECLARED_FIELDS)
                .withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
        }

        hints.reflection().registerType(CachingAutoConfiguration::class.java)
        hints.reflection().registerType(JdbcCachingAutoConfiguration::class.java)
        hints.reflection().registerType(JdbcTransactionInjection::class.java)
        hints.reflection().registerType(NoopTransactionInjection::class.java)
        hints.reflection().registerType(TypeReference.of("com.esotericsoftware.kryo.Kryo"))
        hints.reflection().registerType(TypeReference.of("com.google.common.annotations.concurrent.ConcurrentHashMap"))
    }
}