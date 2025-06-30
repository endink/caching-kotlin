package com.labijie.caching

import java.time.Duration
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2025/6/24
 */
interface ICacheItem {

    fun getKotlinType(): KType?

    fun getData(): Any

    fun getExpiration(): Duration?

    companion object {
        fun of(data: Any,  type: KType?, expiration: Duration? = null): ICacheItem {
            return KotlinCacheItem(data,  expiration, type)
        }

        inline fun <reified T: Any> of(data: T, expiration: Duration? = null): ICacheItem {
            return KotlinCacheItem(data, expiration,typeOf<T>())
        }
    }

    data class KotlinCacheItem(private val cacheData: Any, private val expiration: Duration?, private val type: KType?) : ICacheItem {
        override fun getKotlinType(): KType? {
            return type
        }

        override fun getData(): Any {
            return cacheData
        }

        override fun getExpiration(): Duration? {
            return expiration
        }
    }
}