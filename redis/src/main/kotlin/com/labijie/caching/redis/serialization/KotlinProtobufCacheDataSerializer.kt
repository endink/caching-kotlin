package com.labijie.caching.redis.serialization

import com.labijie.caching.CacheSerializationUnsupportedException
import com.labijie.caching.redis.ICacheDataSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import java.lang.reflect.Type
import kotlin.reflect.KType

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2025/6/24
 */
class KotlinProtobufCacheDataSerializer : ICacheDataSerializer {
    companion object {
        const val NAME = "kotlin-protobuf"
    }

    override val name: String
        get() = NAME

    override fun deserializeData(type: Type, data: ByteArray): Any? {
        throw CacheSerializationUnsupportedException("KotlinProtobufCacheDataSerializer only support set data with KType.")
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serializeData(data: Any, kotlinType: KType?): ByteArray {
        val type = kotlinType ?: throw CacheSerializationUnsupportedException("KotlinProtobufCacheDataSerializer only support set data with KType.")
        return ProtoBuf.encodeToByteArray(serializer(type), data)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserializeData(type: KType, data: ByteArray): Any? {
        return ProtoBuf.decodeFromByteArray(serializer(type), data)
    }
}