package com.labijie.caching.redis.configuration

import java.time.Duration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
class RedisRegionOptions {
    var uri: String = ""
    var timeout: Duration = Duration.ofSeconds(10)
    var serializer: String = ""
    var password: String = ""
    var database: Int = 0
}