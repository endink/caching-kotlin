package com.labijie.caching.redis

import com.labijie.caching.CacheConnectionException
import com.labijie.caching.CacheException
import io.lettuce.core.RedisConnectionException
import io.lettuce.core.RedisException

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-22
 */
fun RedisException.wrap(error: String): CacheException {
    val ex = this
    return when (ex) {
        is RedisConnectionException -> CacheConnectionException(error, ex)
        else -> CacheException(error, ex)
    }
}