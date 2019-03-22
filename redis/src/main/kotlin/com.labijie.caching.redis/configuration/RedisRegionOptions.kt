package com.labijie.caching.redis.configuration

import com.labijie.caching.redis.JacksonCacheDataSerializer
import java.time.Duration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
data class RedisRegionOptions(
    var name:String = "",
    var url: String = "",
    var timeout: Duration = Duration.ofSeconds(10),
    var serializer: String = JacksonCacheDataSerializer.NAME,
    var password: String = ""
) {

}