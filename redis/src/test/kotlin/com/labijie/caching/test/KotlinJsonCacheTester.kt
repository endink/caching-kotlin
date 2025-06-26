package com.labijie.caching.test

import com.labijie.caching.redis.serialization.KotlinJsonCacheDataSerializer

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2025/6/26
 */
class KotlinJsonCacheTester: RedisCacheManagerTester() {
    override fun getSerializerName() = KotlinJsonCacheDataSerializer.NAME
}