package com.labijie.caching.redis.configuration

import java.time.Duration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
data class RedisRegionOptions(
    /**
     * redis://[password@]host[:port][/database]
     */
    var url: String = "redis://localhost:6379",
    var timeout: Duration = Duration.ofSeconds(10),
    var serializer: String = "",
    var password: String = ""
)