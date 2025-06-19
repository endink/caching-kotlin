package com.labijie.caching.memory

import com.labijie.caching.*
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-02
 */
class MemoryCacheManager(options: MemoryCacheOptions? = null) : ICacheManager {
    private var caches: ConcurrentHashMap<String, HashSet<String>> = ConcurrentHashMap()
    private var cache: MemoryCache = MemoryCache(options ?: MemoryCacheOptions())

    private fun validateRegion(region: String?) {
        if (region.isNullOrBlank()) {
            return
        }
        if (region.contains("|")) {
            throw CacheException("Cache region name cant be contains \"|\" .")
        }
    }

    private fun getFullKey(region: String?, key: String): String {
        if(key.isBlank()){
            throw CacheException("Key cant not be empty string.")
        }
        val r = getRegionName(region)
        return "$r|$key"
    }

    private fun getRegionName(region: String?): String {
        return if (region.isNullOrBlank()) DEFAULT_REGION_NAME else region.trim { it <= ' ' }
    }

    private fun getRegionNameFormFullKey(fullKey: String): String {
        return fullKey.split("|").dropLastWhile { it.isEmpty() }.toTypedArray()[0]
    }

    private fun createTimeoutOptions(cacheTime: Long?, useSlidingExpiration: Boolean): MemoryCacheEntryOptions {
        val options = MemoryCacheEntryOptions()
        if (!useSlidingExpiration) {
            options.setAbsoluteExpirationRelativeToNow(cacheTime)
        } else {
            options.slidingExpirationMilliseconds = cacheTime
        }
        return options
    }

    private fun callback(key: Any, reason: EvictionReason) {
        val stringKey = key.toString()
        val region = this.getRegionNameFormFullKey(stringKey)
        when (reason) {
            EvictionReason.Capacity, EvictionReason.Removed, EvictionReason.Expired -> {
                val regionKeys = caches[region]
                regionKeys?.remove(stringKey)
            }
            else -> {
            }
        }
    }

    override fun get(key: String, valueType: Type, region: String?): Any? {
        return getCore(key, region)
    }

    private fun getCore(key: String,region: String?): Any? {
        this.validateRegion(region)

        val name = getRegionName(region)
        val fullKey = this.getFullKey(name, key)

        return cache.get(fullKey)
    }

    override fun set(
        key: String,
        data: Any,
        expireMills: Long?,
        timePolicy: TimePolicy,
        region: String?
    ) {
        this.validateRegion(region)

        val name = getRegionName(region)
        val fullKey = this.getFullKey(name, key)

        val regionKeys = caches.computeIfAbsent(name) { HashSet() }
        regionKeys.add(fullKey)
        val options = createTimeoutOptions(expireMills, timePolicy == TimePolicy.Sliding)
        options.postEvictionCallbacks.add(PostEvictionCallbackRegistration(object : IPostEvictionCallback {
            override fun callback(key: Any, value: Any, reason: EvictionReason, state: Any?) {
                (state as MemoryCacheManager).callback(key, reason)
            }
        }, this))
        cache.set(fullKey, data, options)
    }

    override fun setMulti(
        keyAndValues: Map<String, Any>,
        expireMills: Long?,
        timePolicy: TimePolicy,
        region: String?
    ) {
        keyAndValues.forEach { (key, value) ->
            set(key, value, expireMills, timePolicy, region)
        }
    }

    override fun remove(key: String, region: String?): Boolean {
        this.validateRegion(region)

        val name = getRegionName(region)
        val fullKey = this.getFullKey(name, key)

        return cache.remove(fullKey)
    }

    override fun removeMulti(keys: Iterable<String>, region: String?): Int {
        this.validateRegion(region)
        var count = 0
        keys.forEach { key ->
            val name = getRegionName(region)
            val fullKey = this.getFullKey(name, key)

            count += if(cache.remove(fullKey)) 1 else 0
        }
        return count
    }

    override fun refresh(key: String, region: String?): Boolean {
        val result = this.getCore(key, region)
        return result != null
    }

    override fun clearRegion(region: String) {
        this.validateRegion(region)

        val name = getRegionName(region)
        val regionKeys = caches.remove(name)
        if (regionKeys != null) {
            for (v in regionKeys) {
                cache.remove(v)
            }
        }
    }

    override fun clear() {
        val keys = this.caches.keys.toArray()
        keys.forEach {
            val regionKeys = caches.remove(it)
            if (regionKeys != null) {
                for (v in regionKeys) {
                    cache.remove(v)
                }
            }
        }
    }

    companion object {

        const val DEFAULT_REGION_NAME = "__default"
    }
}