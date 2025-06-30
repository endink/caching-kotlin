/**
 * @author Anders Xiao
 * @date 2025-06-16
 */

@file:Suppress("unused")

package com.labijie.caching

import org.slf4j.LoggerFactory
import java.lang.reflect.Type
import java.time.Duration
import kotlin.reflect.KClass
import kotlin.reflect.typeOf


fun <T> ICacheManager.get(key: String, valueType: TypeReference<T>, region: String? = null): Any? {
    val type = valueType.type
    return this.get(key, type, region)
}

fun <T : Any> ICacheManager.get(key: String, valueType: KClass<T>, region: String? = null): T? {
    return this.get(key, valueType.java, region).cast(valueType.java)
}

inline fun <reified T : Any> ICacheManager.get(key: String, region: String? = null): T? {
    return this.get(key, typeOf<T>(), region) as? T
}

inline fun <reified T : Any> ICacheManager.set(
    key: String,
    data: T,
    expireMills: Long? = null,
    timePolicy: TimePolicy = TimePolicy.Absolute,
    region: String? = null,
    serializer: String? = null
) {
    this.set(key, data, typeOf<T>(), expireMills, timePolicy, region, serializer)
}


private val cacheLogger by lazy {
    LoggerFactory.getLogger(ICacheManager::class.java)
}


private fun <T> Any?.cast(javaType: Type?): T? {
    if (this == null) return null

    if (javaType is Class<*>) {
        if (javaType.isInstance(this)) {
            @Suppress("UNCHECKED_CAST")
            return this as? T
        } else {
            cacheLogger.warn("Invalid cache value type, excepted is ${javaType.name} but got others.")
            return null
        }
    }

    @Suppress("UNCHECKED_CAST")
    return this as? T
}


/**
 * 如果缓存中存在指定键的缓存项则从缓存中获取该项，如果不存在，使用指定的工厂方法创建并加入到缓存。
 * @param key 要获取的缓存键。
 * @param javaType 要获取的缓存的值类型。
 * @param factory 当键不存在时用于创建对象的工厂方法。
 * @param expireMills 当发生添加缓存项时用于设置缓存项的过期时间。
 * @param region 要从中获取缓存的缓存区域。
 * @param timePolicy 是否使用过期时间策略。
 * @param <T> 缓存对象类型参数。
 * @return 从缓存中获取到的或新创建的缓存。
 */
private fun <T> ICacheManager.getOrSetInternal(
    key: String,
    factory: (key: String) -> T?,
    javaType: Type,
    expireMills: Long? = null,
    timePolicy: TimePolicy = TimePolicy.Absolute,
    region: String? = null,
    preventException: Boolean = true,
    serializer: String? = null,
): T? {
    var data = try {
        get(key, javaType, region)
    } catch (ex: CacheException) {
        if (!preventException || ex is CacheSerializationUnsupportedException) {
            throw ex
        } else {
            cacheLogger.error("Get cache data fault.", ex)
        }
        null
    }
    if (data == null) {
        data = factory.invoke(key)
        if (data != null) {
            try {
                set(key, data, expireMills, timePolicy, region, serializer)
            } catch (ex: CacheException) {
                if (!preventException) {
                    throw ex
                } else {
                    cacheLogger.error("Set cache data fault.", ex)
                }
            }
        }
    }
    return data.cast(javaType)
}


fun <T : Any> ICacheManager.getOrSet(
    key: String,
    region: String?,
    valueType: Type,
    expiration: Duration,
    timePolicy: TimePolicy = TimePolicy.Absolute,
    preventException: Boolean = true,
    serializer: String? = null,
    factory: (key: String) -> T?,
): T? {

    return getOrSetInternal(
        key,
        factory,
        valueType,
        expiration.toMillis(),
        timePolicy,
        region,
        preventException,
        serializer
    )
}


fun <T : Any> ICacheManager.getOrSet(
    key: String,
    valueType: Type,
    expiration: Duration,
    timePolicy: TimePolicy = TimePolicy.Absolute,
    preventException: Boolean = true,
    serializer: String? = null,
    factory: (key: String) -> T?,
): T? {
    return getOrSet(key, null, valueType, expiration, timePolicy, preventException, serializer, factory)
}

inline fun <reified T : Any> ICacheManager.getOrSet(
    key: String,
    region: String?,
    expiration: Duration,
    timePolicy: TimePolicy = TimePolicy.Absolute,
    serializer: String? = null,
    preventException: Boolean = true,
    factory: (key: String) -> T?,
): T? {

    var data = try {
        get(key, typeOf<T>(), region)
    } catch (ex: CacheException) {
        if (!preventException || ex is CacheSerializationUnsupportedException) {
            throw ex
        } else {
            LoggerFactory.getLogger(ICacheManager::class.java).error("Get cache data fault.", ex)
        }
        null
    }
    if (data == null) {
        data = factory.invoke(key)
        if (data != null) {
            try {
                set(key, data, expiration.toMillis(), timePolicy, region, serializer)
            } catch (ex: CacheException) {
                if (!preventException) {
                    throw ex
                } else {
                    LoggerFactory.getLogger(ICacheManager::class.java).error("Set cache data fault.", ex)
                }
            }
        }
    }

    return when (data) {
        null -> null
        is T -> data
        else -> {
            LoggerFactory.getLogger(ICacheManager::class.java)
                .warn("Cast cache data failed: ${data::class} cannot be cast to ${T::class}")
            null
        }
    }
}


inline fun <reified T : Any> ICacheManager.getOrSet(
    key: String,
    expiration: Duration,
    timePolicy: TimePolicy = TimePolicy.Absolute,
    serializer: String? = null,
    preventException: Boolean = true,
    factory: (key: String) -> T?,
): T? {
    return getOrSet(key, null, expiration, timePolicy, serializer, preventException, factory)
}


fun <T : Any> ICacheManager.getOrSetSliding(
    key: String,
    region: String?,
    valueType: Type,
    slidingExpire: Duration,
    serializer: String? = null,
    preventException: Boolean = true,
    factory: (key: String) -> T?
): T? {

    return this.getOrSetInternal(
        key,
        factory,
        valueType,
        slidingExpire.toMillis(),
        TimePolicy.Sliding,
        region,
        preventException,
        serializer
    )
}

inline fun <reified T : Any> ICacheManager.getOrSetSliding(
    key: String,
    region: String?,
    slidingExpire: Duration,
    serializer: String? = null,
    preventException: Boolean = true,
    factory: (key: String) -> T?
) = this.getOrSet(key, region, slidingExpire, TimePolicy.Sliding, serializer, preventException, factory)

inline fun <reified T : Any> ICacheManager.getOrSetSliding(
    key: String,
    slidingExpire: Duration,
    serializer: String? = null,
    preventException: Boolean = true,
    factory: (key: String) -> T?
) = this.getOrSetSliding(key, null, slidingExpire, serializer, preventException, factory)

