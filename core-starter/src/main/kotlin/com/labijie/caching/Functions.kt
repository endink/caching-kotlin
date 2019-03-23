package com.labijie.caching

import com.labijie.caching.CacheException

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-23
 */
fun cacheScope(vararg prevent: CacheOperation):AutoCloseable{
    val current = ICacheScopeHolder.Current
    if(current != null) {
        return CacheScopeObject(current, prevent = *prevent)
    }else{
        throw CacheException("${ICacheScopeHolder::class.java.simpleName} bean could not be found or it was not ready.")
    }
}

