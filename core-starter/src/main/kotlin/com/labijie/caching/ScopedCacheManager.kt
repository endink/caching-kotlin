package com.labijie.caching

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import java.lang.reflect.Type
import kotlin.reflect.KType

/**
 *
 * @Author: Anders Xiao
 * @Date: 2021/12/10
 * @Description:
 */
class ScopedCacheManager(private val cacheManager: ICacheManager): ICacheManager, ApplicationContextAware {
    private lateinit var context: ApplicationContext

    private val cacheScopeHolder: ICacheScopeHolder by lazy {
        if(this::context.isInitialized){
            context.getBean(ICacheScopeHolder::class.java)
        }else {
            ThreadLocalCacheScopeHolder()
        }
    }

    override fun set(
        key: String,
        data: Any,
        kotlinType: KType?,
        expireMills: Long?,
        timePolicy: TimePolicy,
        region: String?,
        serializer: String?
    ) {
        if(cacheScopeHolder.cacheRequired(CacheOperation.Set)) {
            return cacheManager.set(key, data, kotlinType, expireMills, timePolicy, region, serializer)
        }
    }

    override fun setMulti(
        keyAndValues: Map<String, ICacheItem>,
        expireMills: Long?,
        timePolicy: TimePolicy,
        region: String?,
        serializer: String?
    ) {
        if(cacheScopeHolder.cacheRequired(CacheOperation.Set)) {
            cacheManager.setMulti(keyAndValues, expireMills, timePolicy, region)
        }
    }


    override fun remove(key: String, region: String?): Boolean {
        return if(cacheScopeHolder.cacheRequired(CacheOperation.Remove)) {
            this.cacheManager.remove(key, region)
        }else false
    }

    override fun removeMulti(keys: Iterable<String>, region: String?): Int {
        return if(cacheScopeHolder.cacheRequired(CacheOperation.Remove)) {
            this.cacheManager.removeMulti(keys, region)
        }else 0
    }

    override fun refresh(key: String, region: String?): Boolean {
        if(cacheScopeHolder.cacheRequired(CacheOperation.Set)) {
            return this.cacheManager.refresh(key, region)
        }
        return false
    }

    override fun clearRegion(region: String) {
        if(cacheScopeHolder.cacheRequired(CacheOperation.Remove)) {
            return this.cacheManager.clearRegion(region)
        }
    }

    override fun clear() {
        if(cacheScopeHolder.cacheRequired(CacheOperation.Remove)) {
            return this.cacheManager.clear()
        }
    }

    override fun get(key: String, valueType: Type, region: String?): Any? {
        if(cacheScopeHolder.cacheRequired(CacheOperation.Get)) {
            return this.cacheManager.get(key, valueType, region)
        }
        return null
    }

    override fun get(key: String, valueType: KType, region: String?): Any? {
        if(cacheScopeHolder.cacheRequired(CacheOperation.Get)) {
            return this.cacheManager.get(key, valueType, region)
        }
        return null
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext
    }
}