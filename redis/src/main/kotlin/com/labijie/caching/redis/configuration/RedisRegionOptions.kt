package com.labijie.caching.redis.configuration

import com.labijie.caching.redis.serialization.JacksonCacheDataSerializer
import java.time.Duration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
data class RedisRegionOptions(
    var url: String = "",
    var timeout: Duration = Duration.ofSeconds(10),
    var serializer: String = "",
    var password: String = ""
)