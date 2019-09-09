package com.labijie.caching.redis.testing

import com.labijie.caching.redis.serialization.KryoCacheDataSerializer
import com.labijie.caching.redis.testing.configuration.KryoCacheManagerFactory
import org.springframework.context.annotation.Import

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-09
 */
@Import(KryoCacheManagerFactory::class)
class CacheAnnotationKryoTester: CacheAnnotationTester() {
    override fun getSerializerName(): String {
        return KryoCacheDataSerializer.NAME
    }
}