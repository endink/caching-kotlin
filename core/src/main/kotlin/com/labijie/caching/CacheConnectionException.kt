package com.labijie.caching

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-22
 */
open class CacheConnectionException(message: String, cause: Throwable? = null, enableSuppression: Boolean = false, writableStackTrace: Boolean = true)
    : CacheException(message, cause, enableSuppression, writableStackTrace) {
}