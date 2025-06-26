@file:Suppress("UNCHECKED_CAST")

package com.labijie.caching.redis

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.labijie.caching.CacheConnectionException
import com.labijie.caching.CacheException
import com.labijie.caching.ICacheManager
import com.labijie.caching.TimePolicy
import io.lettuce.core.RedisConnectionException
import io.lettuce.core.RedisException
import java.lang.reflect.Type
import java.util.function.Function
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-22
 */
fun RedisException.wrap(error: String): CacheException {
    return when (val ex = this) {
        is RedisConnectionException -> CacheConnectionException(error, ex)
        else -> CacheException(error, ex)
    }
}

