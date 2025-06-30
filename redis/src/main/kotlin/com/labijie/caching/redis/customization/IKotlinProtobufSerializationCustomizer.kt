package com.labijie.caching.redis.customization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBufBuilder

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2025/6/30
 */
interface IKotlinProtobufSerializationCustomizer {
    @OptIn(ExperimentalSerializationApi::class)
    fun customize(builder: ProtoBufBuilder)
}