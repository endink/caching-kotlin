package com.labijie.caching

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

    companion object {
        fun of(data: Any, type: KType? = null): ICacheItem {
            return KotlinCacheItem(data, type)
        }

        inline fun <reified T: Any> of(data: T): ICacheItem {
            return KotlinCacheItem(data, typeOf<T>())
        }
    }

    data class KotlinCacheItem(
        val cacheData: Any,
        val type: KType?,
    ) : ICacheItem {
        override fun getKotlinType(): KType? {
            return type
        }

        override fun getData(): Any {
            return cacheData
        }
    }
}