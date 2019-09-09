package com.labijie.caching.test

import com.labijie.caching.redis.serialization.JacksonCacheDataSerializer

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-09
 */
class JacksonCacheTester : RedisCacheManagerTester() {
    override fun getSerializerName() = JacksonCacheDataSerializer.NAME
}