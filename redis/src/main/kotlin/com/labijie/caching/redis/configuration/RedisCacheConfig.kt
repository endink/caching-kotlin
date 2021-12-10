package com.labijie.caching.redis.configuration

import com.labijie.caching.redis.serialization.JacksonCacheDataSerializer
import com.labijie.caching.redis.serialization.JsonSmileDataSerializer

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
open class RedisCacheConfig {
    var defaultRegion: String = ""
    var defaultSerializer: String = JsonSmileDataSerializer.NAME
    var regions: MutableMap<String, RedisRegionOptions> = mutableMapOf()
}