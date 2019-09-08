package com.labijie.caching

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-23
 */
fun <T> suppressCache(vararg options: CacheOperation, action: () -> T): T {
    val current = ICacheScopeHolder.Current
    if (current != null) {
        return CacheScopeObject(current, suppressed = *options).use {
            action()
        }
    } else {
        throw CacheException("${ICacheScopeHolder::class.java.simpleName} bean could not be found or it was not ready.")
    }
}

