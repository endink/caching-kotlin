package com.labijie.caching

import org.slf4j.LoggerFactory
import java.lang.reflect.Type
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
     * 将对象以指定的缓键添加到缓存, 如果已存在键为 key 的缓存对象 ，则更新此对象。
     * @param key 缓存键。
     * @param data 要添加到缓存的对象。
     * @param expireMills  缓存过期时间， 为空表示永不过期（单位：毫秒）。
     * @param region 缓存区域。
     * @param timePolicy 指示是否使用的过期时间策略。
     */
    @Throws(CacheException::class)
    fun set(
        key: String,
        data: Any,
        expireMills: Long? = null,
        timePolicy: TimePolicy = TimePolicy.Absolute,
        region: String? = null
    )

    /**
     * 从缓存中移除指定键的缓存实例。
     * @param key 要移除的缓存实例的键值。
     * @param region 缓存区域。
     */
    @Throws(CacheException::class)
    fun remove(key: String, region: String? = null)

    /**
     * 表示对滑动过期时间的缓存重新计时（绝对过期时间该操作无效）。
     * @param key 要刷新的缓存实例的键值。
     * @param region 缓存区域。
     * @return 返回一个布尔值，指示是否缓存对象被刷新（如果缓存中未找到对象会返回 null）。
     */
    @Throws(CacheException::class)
    fun refresh(key: String, region: String? = null): Boolean

    /**
     * 清空指定区域的缓存。
     * @param region 要清理的缓存区域。
     */
    @Throws(CacheException::class)
    fun clearRegion(region: String)

    /**
     * 清理所有的缓存。
     */
    @Throws(CacheException::class)
    fun clear()

    /**
     * 根据指定的缓存键获取缓存实例。
     * @param key 缓存键。
     * @param region 缓存区域（可以为空或空串）。
     * @param valueType 缓存项的类型 。
     * @return 缓存键对应的缓存实例。为空表示缓存中不存在键为 key 的对象。
     */
    @Throws(CacheException::class)
    fun get(key: String, valueType: Type, region: String? = null): Any?
}
