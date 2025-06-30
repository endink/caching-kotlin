package com.labijie.caching.redis.serialization

import com.labijie.caching.CacheSerializationUnsupportedException
import com.labijie.caching.redis.CacheDataSerializationException
import com.labijie.caching.redis.ICacheDataSerializer
import com.labijie.caching.redis.customization.IKotlinCacheDataSerializerCustomizer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import java.lang.reflect.Type
import kotlin.reflect.KType

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2025/6/24
 */
class KotlinJsonCacheDataSerializer @JvmOverloads constructor(private val customizers: Iterable<IKotlinCacheDataSerializerCustomizer> = emptyList()) : ICacheDataSerializer {
    companion object {
        const val NAME = "kotlin-json"
    }

    override val name: String
        get() = NAME

    override fun deserializeData(type: Type, data: ByteArray): Any? {
        throw CacheSerializationUnsupportedException("KotlinJsonCacheDataSerializer only support set data with KType.")
    }

    private val json by lazy {
        Json {
            if(customizers.any()) {
                this.serializersModule = SerializersModule {
                    customizers.forEach {
                        it.customize(this)
                    }
                }
            }
        }
    }

    override fun serializeData(data: Any, kotlinType: KType?): ByteArray {
        val type = kotlinType ?: throw CacheSerializationUnsupportedException("KotlinJsonCacheDataSerializer only support set data with KType.")
        try {
            return json.encodeToString(serializer(type), data).toByteArray(Charsets.UTF_8)
        }catch (e: Throwable) {
            throw CacheDataSerializationException("Could not serialize data (kotlin json serializer)", e)
        }
    }

    override fun deserializeData(type: KType, data: ByteArray): Any? {
        try {
            return json.decodeFromString(serializer(type), data.toString(Charsets.UTF_8))
        }catch (e: Throwable) {
            throw CacheDataSerializationException("Could not deserialize data (kryo serializer)", e)
        }

    }
}