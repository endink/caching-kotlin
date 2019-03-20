package com.labijie.caching.redis.configuration

import java.time.Duration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
class RedisCacheConfig {
    var defaultRegion: String = ""
    var regions: MutableMap<String, RedisRegionOptions> = mutableMapOf()
}