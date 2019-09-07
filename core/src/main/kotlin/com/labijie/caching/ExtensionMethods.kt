@file:Suppress("UNCHECKED_CAST")

package com.labijie.caching

import org.slf4j.LoggerFactory
import java.lang.reflect.Type
import java.time.Duration
import java.util.function.Function
import kotlin.reflect.KClass

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-07
 */

/**
 * 如果缓存中存在指定键的缓存项则从缓存中获取该项，如果不存在，使用指定的工厂方法创建并加入到缓存。
 * @param key 要获取的缓存键。
 * @param valueClass 要获取的缓存的值类型。
 * @param factory 当键不存在时用于创建对象的工厂方法。
 * @param expireMills 当发生添加缓存项时用于设置缓存项的过期时间。
 * @param region 要从中获取缓存的缓存区域。
 * @param timePolicy 是否使用过期时间策略。
 * @param <T> 缓存对象类型参数。
 * @return 从缓存中获取到的或新创建的缓存。
 */
@Suppress("UNCHECKED_CAST")
private fun <T> ICacheManager.getOrSet(
    key: String,
    valueType: Type,
    factory: Function<String, T?>,
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
            LoggerFactory.getLogger(ICacheManager::class.java).warn("Get cache data fault.", ex)
        }
    }
    if (data == null) {
        data = factory.apply(key)
        if (data != null) {
            try {
                set(key, data, expireMills, timePolicy, region)
            } catch (ex: CacheException) {
                if (!preventException) {
                    throw ex
                } else {
                    LoggerFactory.getLogger(ICacheManager::class.java).warn("Set cache data fault.", ex)
                }
            }
        }
    }
    return data as? T
}

@Deprecated("Use another getXXX method instead.")
fun <T> ICacheManager.getOrSet(
    key: String,
    factory: Function<String, T?>,
    expireMills: Long? = null,
    timePolicy: TimePolicy = TimePolicy.Absolute,
    region: String? = null,
    preventException: Boolean = true
): T? {
    val method = factory::class.java.getDeclaredMethod("apply", String::class.java)
    val valueType = method.genericReturnType
    return this.getOrSet(key, valueType,factory, expireMills, timePolicy, region, preventException)
}


fun <T : Any> ICacheManager.get(key: String, valueType: KClass<T>, region: String? = null): T? {
    return this.get(key, valueType.java, region) as? T
}


fun <T : Any> ICacheManager.getOrSet(
    key: String,
    region: String?,
    absoluteExpire: Duration,
    factory: (key: String) -> T?
): T? {
    val method = factory::class.java.getDeclaredMethod("invoke", String::class.java)
    val valueType = method.genericReturnType

    return this.getOrSet(
        key,
        valueType,
        Function { t -> factory.invoke(t) },
        absoluteExpire.toMillis(),
        TimePolicy.Absolute,
        region,
        true
    )
}

fun <T : Any> ICacheManager.getOrSet(
    key: String,
    absoluteExpire: Duration,
    factory: (key: String) -> T?
) = this.getOrSet(key, null, absoluteExpire, factory)

fun <T : Any> ICacheManager.getOrSetSliding(
    key: String,
    region: String?,
    slidingExpire: Duration,
    factory: (key: String) -> T?
): T? {

    val method = factory::class.java.getDeclaredMethod("invoke", String::class.java)
    val valueType = method.genericReturnType

    return this.getOrSet(
        key,
        valueType,
        Function { t -> factory.invoke(t) },
        slidingExpire.toMillis(),
        TimePolicy.Sliding,
        region,
        true
    )
}

fun <T : Any> ICacheManager.getOrSetSliding(
    key: String,
    slidingExpire: Duration,
    factory: (key: String) -> T?
) = this.getOrSetSliding(key, null, slidingExpire, factory)