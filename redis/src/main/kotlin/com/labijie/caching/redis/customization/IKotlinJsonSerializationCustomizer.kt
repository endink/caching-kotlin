package com.labijie.caching.redis.customization

import kotlinx.serialization.json.JsonBuilder

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2025/6/30
 */
interface IKotlinJsonSerializationCustomizer {
    fun customize(builder: JsonBuilder)
}