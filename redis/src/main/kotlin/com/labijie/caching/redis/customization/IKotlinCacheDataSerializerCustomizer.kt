package com.labijie.caching.redis.customization

import kotlinx.serialization.modules.SerializersModuleBuilder

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2025/6/30
 */
interface IKotlinCacheDataSerializerCustomizer {
    fun customize(builder: SerializersModuleBuilder)
}