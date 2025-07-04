package com.labijie.caching.redis.customization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlin.reflect.KType

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2025/6/30
 */
@JvmDefaultWithCompatibility
interface IKotlinCacheDataSerializerCustomizer {
    fun customSerializers(): Map<KType, KSerializer<Any?>>
}