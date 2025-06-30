package com.labijie.caching.redis.customization

import kotlinx.serialization.KSerializer
import kotlin.reflect.KType

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2025/6/30
 */
data class KotlinSerializerRegistrar(val type: KType, val serializer: KSerializer<*>)