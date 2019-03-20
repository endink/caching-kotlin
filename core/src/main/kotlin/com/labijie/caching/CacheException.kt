package com.labijie.caching

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
open class CacheException(message: String? = null, cause: Throwable? = null, enableSuppression: Boolean = false, writableStackTrace: Boolean = true)
    : RuntimeException(message, cause, enableSuppression, writableStackTrace) {

    constructor(message:String) : this(message, null, false, true)
}