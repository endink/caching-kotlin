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


private fun <T> Any?.cast(valueType: Type): T? {
    if(this == null) return null

    if (valueType is Class<*>) {
        if (valueType.isInstance(this)) {
            @Suppress("UNCHECKED_CAST")
            return this as? T
        }else {
            LoggerFactory.getLogger(ICacheManager::class.java).warn("Invalid cache value type, excepted is ${valueType.name} but got others.")
            return null
        }
    }

    @Suppress("UNCHECKED_CAST")
    return this as? T
}


internal inline fun <reified T> Any?.cast(): T? {
    return this.cast(T::class.java)
}

inline fun <reified T> typedSupplier(noinline block: (String) -> T): TypedCacheValueSupplier<T> {
    val type = object : TypeReference<T>() {}.type
    return object : TypedCacheValueSupplier<T> {
        override val returnType: Type = type
        override val supplier = block
    }
}


/**
 * 如果缓存中存在指定键的缓存项则从缓存中获取该项，如果不存在，使用指定的工厂方法创建并加入到缓存。
 * @param key 要获取的缓存键。
 * @param valueType 要获取的缓存的值类型。
 * @param factory 当键不存在时用于创建对象的工厂方法。
 * @param expireMills 当发生添加缓存项时用于设置缓存项的过期时间。
 * @param region 要从中获取缓存的缓存区域。
 * @param timePolicy 是否使用过期时间策略。
 * @param <T> 缓存对象类型参数。
 * @return 从缓存中获取到的或新创建的缓存。
 */
private fun <T> ICacheManager.getOrSetInternal(
    key: String,
    valueType: Type,
    factory: (key: String) -> T?,
    expireMills: Long? = null,
    timePolicy: TimePolicy = TimePolicy.Absolute,
    region: String? = null,
    preventException: Boolean = true
): T? {
    var data = try {
        get(key, valueType, region)
    } catch (ex: CacheException) {
        if (!preventException) {
            throw ex
        } else {
            LoggerFactory.getLogger(ICacheManager::class.java).error("Get cache data fault.", ex)
        }
    }
    if (data == null) {
        data = factory.invoke(key)
        if (data != null) {
            try {
                set(key, data, expireMills, timePolicy, region)
            } catch (ex: CacheException) {
                if (!preventException) {
                    throw ex
                } else {
                    LoggerFactory.getLogger(ICacheManager::class.java).error("Set cache data fault.", ex)
                }
            }
        }
    }
    return data.cast(valueType)
}



fun <T : Any> ICacheManager.get(key: String, valueType: KClass<T>, region: String? = null): T? {
    return this.get(key, valueType.java, region).cast(valueType.java)
}

inline fun <reified T : Any> ICacheManager.get(key: String, region: String? = null): T? {
    val type = object : TypeReference<T>() {}.type
    return this.get(key, type, region) as? T
}


fun <T : Any> ICacheManager.getOrSet(
    key: String,
    region: String?,
    absoluteExpire: Duration,
    valueType: Type,
    factory: (key: String) -> T?
): T? {

    return this.getOrSetInternal(
        key,
        valueType,
        factory,
        absoluteExpire.toMillis(),
        TimePolicy.Absolute,
        region,
        true
    )
}

fun <T : Any> ICacheManager.getOrSet(
    key: String,
    absoluteExpire: Duration,
    valueType: Type,
    factory: (key: String) -> T?
) = this.getOrSet(key, null, absoluteExpire, valueType, factory)



fun <T : Any> ICacheManager.getOrSetSliding(
    key: String,
    region: String?,
    slidingExpire: Duration,
    valueType: Type,
    factory: (key: String) -> T?
): T? {

    return this.getOrSetInternal(
        key,
        valueType,
        factory,
        slidingExpire.toMillis(),
        TimePolicy.Sliding,
        region,
        true
    )
}

fun <T : Any> ICacheManager.getOrSetSliding(
    key: String,
    slidingExpire: Duration,
    valueType: Type,
    factory: (key: String) -> T?
) = this.getOrSetSliding(key, null, slidingExpire, valueType, factory)


inline fun <reified T : Any> ICacheManager.getOrSet(
    key: String,
    region: String?,
    absoluteExpire: Duration,
    noinline factory: (String) -> T?,
): T? {
    val supplier = typedSupplier { key -> factory(key) }
    return this.getOrSet(key, region, absoluteExpire, supplier.returnType, supplier.supplier)
}


inline fun <reified T : Any> ICacheManager.getOrSet(
    key: String,
    absoluteExpire: Duration,
    noinline factory: (key: String) -> T?
): T? {
    val supplier = typedSupplier { key -> factory(key) }
    return this.getOrSet(key, null, absoluteExpire, supplier.returnType, supplier.supplier)
}


inline fun <reified T : Any> ICacheManager.getOrSetSliding(
    key: String,
    region: String?,
    slidingExpire: Duration,
    noinline factory: (String) -> T?,
): T? {
    return this.getOrSetSliding(key, region, slidingExpire, T::class.java, factory)
}

inline fun <reified T : Any> ICacheManager.getOrSetSliding(
    key: String,
    slidingExpire: Duration,
    noinline factory: (String) -> T?,
): T? {
    return this.getOrSetSliding(key, null, slidingExpire, T::class.java, factory)
}
