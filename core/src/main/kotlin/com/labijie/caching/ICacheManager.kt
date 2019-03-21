package com.labijie.caching

import java.util.function.Function
import kotlin.reflect.KClass

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-02
 *
 * 缓存管理接口，接口中的 region 提供逻辑上的分区。
 * Created by sharp on 2017/3/5.
 */
interface ICacheManager {
    /**
     * 根据指定的缓存键获取缓存实例。
     * @param key 缓存键。
     * @param region 缓存区域（可以为空或空串）。
     * @return 缓存键对应的缓存实例。为空表示缓存中不存在键为 key 的对象。
     */
    fun get(key: String, region: String? = null): Any?

    /**
     * 将对象以指定的缓键添加到缓存, 如果已存在键为 key 的缓存对象 ，则更新此对象。
     * @param key 缓存键。
     * @param data 要添加到缓存的对象。
     * @param expireMills  缓存过期时间， 为空表示永不过期（单位：毫秒）。
     * @param region 缓存区域。
     * @param timePolicy 指示是否使用的过期时间策略。
     */
    fun set(key: String, data: Any, expireMills: Long? = null, timePolicy: TimePolicy = TimePolicy.Absolute, region: String? = null)

    /**
     * 从缓存中移除指定键的缓存实例。
     * @param key 要移除的缓存实例的键值。
     * @param region 缓存区域。
     */
    fun remove(key: String, region: String? = null)

    /**
     * 表示对滑动过期时间的缓存重新计时（绝对过期时间该操作无效）。
     * @param key 要刷新的缓存实例的键值。
     * @param region 缓存区域。
     * @return 返回一个布尔值，指示是否缓存对象被刷新（如果缓存中未找到对象会返回 null）。
     */
    fun refresh(key: String, region: String? = null): Boolean

    /**
     * 清空指定区域的缓存。
     * @param region 要清理的缓存区域。
     */
    fun clearRegion(region: String)

    /**
     * 清理所有的缓存。
     */
    fun clear()

    /**
     * 如果缓存中存在指定键的缓存项则从缓存中获取该项，如果不存在，使用指定的工厂方法创建并加入到缓存。
     * @param key 要获取的缓存键。
     * @param factory 当键不存在时用于创建对象的工厂方法。
     * @param expireMills 当发生添加缓存项时用于设置缓存项的过期时间。
     * @param region 要从中获取缓存的缓存区域。
     * @param timePolicy 是否使用过期时间策略。
     * @param <T> 缓存对象类型参数。
     * @return 从缓存中获取到的或新创建的缓存。
    </T> */
    fun <T> getOrSet(
        key: String,
        factory: Function<String, T>,
        expireMills: Long? = null,
        timePolicy: TimePolicy = TimePolicy.Absolute,
        region: String? = null
    ): T? {
        var data: Any? = get(key, region)
        if (data == null) {
            data = factory.apply(key)
            if (data != null) {
                set(key, data, expireMills, timePolicy, region)
            }
        }
        @Suppress("UNCHECKED_CAST")
        return data as? T
    }

    /**
     * 根据指定的缓存键获取缓存实例。
     * @param key 缓存键。
     * @param region 缓存区域（可以为空或空串）。
     * @param valueType 缓存项的类型 。
     * @return 缓存键对应的缓存实例。为空表示缓存中不存在键为 key 的对象。
     */
    fun <T:Any> get(key: String, valueType: KClass<T>, region: String? = null): T? {
        @Suppress("UNCHECKED_CAST")
        return get(key, region) as? T
    }
}
