package com.labijie.caching.redis

import com.labijie.caching.CacheException

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
open class RedisCacheException(message: String, cause: Throwable? = null, enableSuppression: Boolean = false, writableStackTrace: Boolean = true)
    : CacheException(message, cause, enableSuppression, writableStackTrace) {
}