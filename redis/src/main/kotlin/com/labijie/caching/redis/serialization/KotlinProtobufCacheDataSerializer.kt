package com.labijie.caching.redis.serialization

import com.labijie.caching.CacheSerializationUnsupportedException
import com.labijie.caching.redis.CacheDataSerializationException
import com.labijie.caching.redis.ICacheDataSerializer
import com.labijie.caching.redis.customization.IKotlinCacheDataSerializerCustomizer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2025/6/24
 */

@OptIn(ExperimentalSerializationApi::class)
class KotlinProtobufCacheDataSerializer
@JvmOverloads
constructor(private val customizers: Iterable<IKotlinCacheDataSerializerCustomizer> = emptyList()) :
    ICacheDataSerializer {
    companion object {
        const val NAME = "kotlin-protobuf"
    }

    override val name: String
        get() = NAME

    private val wellKnownTypes: Map<KType, KSerializer<Any?>>

    init {

        val maps = mutableMapOf<KType, KSerializer<Any?>>()
        customizers.forEach {
            it.customSerializers().forEach {
                    kv->
                maps[kv.key] = kv.value
            }
        }
        wellKnownTypes = maps
    }

    private val protobuf by lazy {
        ProtoBuf {
            if(wellKnownTypes.isNotEmpty()) {
                this.serializersModule = SerializersModule {
                    wellKnownTypes.forEach { type ->
                        @Suppress("UNCHECKED_CAST")
                        (type.key.classifier as? KClass<*>)?.let {
                            contextual(it as KClass<Any>, type.value as KSerializer<Any>)
                        }
                    }
                }
            }
        }
    }

    override fun deserializeData(type: Type, data: ByteArray): Any? {
        throw CacheSerializationUnsupportedException("KotlinProtobufCacheDataSerializer only support set data with KType.")
    }

    override fun serializeData(data: Any, kotlinType: KType?): ByteArray {
        val type = kotlinType
            ?: throw CacheSerializationUnsupportedException("KotlinProtobufCacheDataSerializer only support set data with KType.")
        try {
            val ser = wellKnownTypes[type] ?: serializer(type)
            return protobuf.encodeToByteArray(ser, data)
        } catch (e: Throwable) {
            throw CacheDataSerializationException("Could not serialize data (kotlin protobuf serializer)", e)
        }
    }

    override fun deserializeData(type: KType, data: ByteArray): Any? {
        try {
            val ser = wellKnownTypes[type] ?: serializer(type)
            return protobuf.decodeFromByteArray(ser, data)
        } catch (e: Throwable) {
            throw CacheDataSerializationException("Could not deserialize data (kotlin protobuf serializer)", e)
        }
    }
}