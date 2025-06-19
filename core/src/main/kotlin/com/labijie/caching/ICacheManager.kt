/**
 * @author Anders Xiao
 * @date 2019-03-02
 */

package com.labijie.caching

import java.lang.reflect.Type


/**
 * Cache manager interface. The `region` parameter provides logical partitioning.
 */
interface ICacheManager {

    /**
     * Adds an object to the cache with the specified key. If a cache entry with the same key already exists, it will be updated.
     * @param key The cache key.
     * @param data The object to be cached.
     * @param expireMills Expiration time in milliseconds. Null means it never expires.
     * @param region Logical cache region.
     * @param timePolicy Indicates the expiration policy (absolute or sliding).
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
     * Adds multiple key-value pairs to the cache at once.
     *
     * In the Redis implementation it is an atomic operation, but in the memory implementation not.
     *
     * @param keyAndValues A map containing keys and their corresponding data to be cached.
     * @param expireMills Expiration time in milliseconds. Null means it never expires.
     * @param region Logical cache region.
     * @param timePolicy Indicates the expiration policy (absolute or sliding).
     */
    @Throws(CacheException::class)
    fun setMulti(
        keyAndValues: Map<String, Any>,
        expireMills: Long? = null,
        timePolicy: TimePolicy = TimePolicy.Absolute,
        region: String? = null
    )

    /**
     * Removes the cache entry with the specified key.
     * @param key The key of the cache entry to be removed.
     * @param region Logical cache region.
     */
    @Throws(CacheException::class)
    fun remove(key: String, region: String? = null): Boolean

    /**
     * Removes multiple cache entries.
     *
     * In the Redis implementation it is an atomic operation, but in the memory implementation not.
     *
     * @param keys A collection of keys to remove.
     * @param region Logical cache region.
     * @return The number of removed entries.
     *
     */
    @Throws(CacheException::class)
    fun removeMulti(keys: Iterable<String>, region: String? = null): Int

    /**
     * Resets the expiration time for a cache entry using sliding expiration (has no effect for absolute expiration).
     * @param key The key of the cache entry to refresh.
     * @param region Logical cache region.
     * @return A boolean indicating whether the cache entry was refreshed (returns false if not found).
     */
    @Throws(CacheException::class)
    fun refresh(key: String, region: String? = null): Boolean

    /**
     * Clears all cache entries in the specified region.
     * @param region The logical region to clear.
     */
    @Throws(CacheException::class)
    fun clearRegion(region: String)

    /**
     * Clears all cache entries across all regions.
     */
    @Throws(CacheException::class)
    fun clear()

    /**
     * Retrieves a cached object by key.
     * @param key The cache key.
     * @param region Logical cache region (can be null or empty).
     * @param valueType The expected type of the cached value.
     * @return The cached object, or null if the key is not found.
     */
    @Throws(CacheException::class)
    fun get(key: String, valueType: Type, region: String? = null): Any?
}
