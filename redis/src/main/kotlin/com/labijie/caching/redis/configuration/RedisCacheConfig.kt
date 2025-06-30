package com.labijie.caching.redis.configuration

import com.labijie.caching.redis.RedisCacheException
import com.labijie.caching.redis.RedisCacheManager.Companion.NULL_REGION_NAME
import com.labijie.caching.redis.serialization.JacksonCacheDataSerializer
import com.labijie.caching.redis.serialization.JsonSmileDataSerializer

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
open class RedisCacheConfig {
    var defaultRegion: String = ""
    var defaultSerializer: String = JacksonCacheDataSerializer.NAME
    var regions: MutableMap<String, RedisRegionOptions> = mutableMapOf()

    companion object {
        fun RedisCacheConfig.getSerializer(region: String?): String {
            val region = getRegionOptions(region)
            return region.options.serializer.ifBlank { defaultSerializer.ifBlank { JacksonCacheDataSerializer.NAME } }
        }

        fun RedisCacheConfig.getRegionOptions(region: String?): NamedOptions
        {
            if (region == NULL_REGION_NAME) {
                throw RedisCacheException("Cache region name can not be '--'.")
            }
            if (regions.isEmpty()) {
                throw RedisCacheException("At least one redis cache region to be configured")
            }
            val name = if (region.isNullOrBlank()) defaultRegion.trim() else region.trim()
            val config = if (name.isBlank()) {
                regions.values.first()
            } else {
                regions.getOrDefault(name, null)
                    ?: throw RedisCacheException("Cant found redis cache region '$name' that be configured")
            }
            return NamedOptions(name, config)
        }
    }

    data class NamedOptions(val name: String, val options: RedisRegionOptions)
}