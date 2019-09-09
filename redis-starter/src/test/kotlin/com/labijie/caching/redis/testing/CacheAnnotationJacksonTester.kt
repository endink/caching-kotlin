package com.labijie.caching.redis.testing

import com.labijie.caching.redis.serialization.JacksonCacheDataSerializer
import com.labijie.caching.redis.testing.configuration.JacksonCacheManagerFactory
import org.springframework.context.annotation.Import

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-09
 */
@Import(JacksonCacheManagerFactory::class)
class CacheAnnotationJacksonTester: CacheAnnotationTester(){
    override fun getSerializerName(): String {
        return JacksonCacheDataSerializer.NAME
    }
}