package com.labijie.caching

import com.labijie.caching.CacheException

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-23
 */
fun cacheScope(vararg prevent: CacheOperation, action:()->Unit){
    val current = ICacheScopeHolder.Current
    if(current != null) {
        CacheScopeObject(current, prevent = *prevent).use {
            action()
        }
    }else{
        throw CacheException("${ICacheScopeHolder::class.java.simpleName} bean could not be found or it was not ready.")
    }
}

