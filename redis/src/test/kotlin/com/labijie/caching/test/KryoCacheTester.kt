package com.labijie.caching.test

import com.labijie.caching.redis.serialization.KryoCacheDataSerializer

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-09
 */
class KryoCacheTester: RedisCacheManagerTester() {
    override fun getSerializerName() = KryoCacheDataSerializer.NAME
}