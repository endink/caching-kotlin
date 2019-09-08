package com.labijie.caching.redis

import com.labijie.caching.CacheException

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-09
 */
class CacheDataSerializationException(message:String, cause:Throwable? = null)
    : CacheException(message, cause) {
}

class CacheDataDeserializationException(message:String, cause:Throwable? = null)
    : CacheException(message, cause) {
}