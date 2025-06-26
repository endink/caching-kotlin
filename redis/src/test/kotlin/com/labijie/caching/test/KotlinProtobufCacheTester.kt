package com.labijie.caching.test

import com.labijie.caching.redis.serialization.KotlinProtobufCacheDataSerializer

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2025/6/26
 */
class KotlinProtobufCacheTester: RedisCacheManagerTester() {
    override fun getSerializerName() = KotlinProtobufCacheDataSerializer.NAME
}